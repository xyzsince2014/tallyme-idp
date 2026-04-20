package tallyme.idp.domain.services.api.v1.token;

import com.nimbusds.jose.util.Base64URL;
import java.security.MessageDigest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tallyme.idp.domain.entities.postgres.Usr;
import tallyme.idp.domain.entities.redis.ProAuthoriseCache;
import tallyme.idp.domain.logics.ClientLogic;
import tallyme.idp.domain.logics.RedisLogic;
import tallyme.idp.domain.logics.TokenLogic;
import tallyme.idp.domain.logics.UsrLogic;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.dtos.CredentialsDto;
import tallyme.idp.dtos.GenerateTokensRequestDto;
import tallyme.idp.dtos.GenerateTokensResponseDto;
import tallyme.idp.dtos.TokenValidationResultDto;
import tallyme.idp.utils.Decorder;

@Service
public class AuthorisationCodeFlowService extends TokenService<ProAuthoriseCache> {

  private final RedisLogic redisLogic;
  private final TokenLogic tokenLogic;
  private final UsrLogic usrLogic;

  private final String codeChallengeMethod; // RFC 7636
  private final String errorInvalidCode;
  private final String errorInvalidCodeChallenge;
  private final String errorInvalidCodeChallengeMethod;
  private final String errorNoMatchingUser;

  @Autowired
  public AuthorisationCodeFlowService(
    ClientLogic clientLogic,
    Decorder decorder,
    RedisLogic redisLogic,
    TokenLogic tokenLogic,
    UsrLogic usrLogic,
    @Value("${error.invalid-client-id}") String errorInvalidClientId,
    @Value("${error.no-matching-client}") String errorNoMatchingClient,
    @Value("${error.no-matching-client-secret}") String errorNoMatchingClientSecret,
    @Value("${oauth.pkce.code-challenge-method}") String codeChallengeMethod,
    @Value("${error.invalid-code}") String errorInvalidCode,
    @Value("${error.invalid-code-challenge}") String errorInvalidCodeChallenge,
    @Value("${error.invalid-code-challenge-method}") String errorInvalidCodeChallengeMethod,
    @Value("${error.no-matching-user}") String errorNoMatchingUser
  ) {
    super(clientLogic, decorder, errorInvalidClientId, errorNoMatchingClient, errorNoMatchingClientSecret);
    this.redisLogic = redisLogic;
    this.tokenLogic = tokenLogic;
    this.usrLogic = usrLogic;
    this.codeChallengeMethod = codeChallengeMethod;
    this.errorInvalidCode = errorInvalidCode;
    this.errorInvalidCodeChallenge = errorInvalidCodeChallenge;
    this.errorInvalidCodeChallengeMethod = errorInvalidCodeChallengeMethod;
    this.errorNoMatchingUser = errorNoMatchingUser;
  }

  /**
   * Validates requests to the token endpoint.
   *
   * @return TokenValidationResultDto
   */
  @Override
  public TokenValidationResultDto<ProAuthoriseCache> execValidation(
    GenerateTokensRequestDto requestDto, String authorization
  ) throws ApiException {

    // validate client credentials in the Authorization header
    CredentialsDto credentialsDto = this.validateClient(requestDto, authorization);

    // look up the ProAuthoriseCache by the given code — if null, the code is invalid or already consumed.
    ProAuthoriseCache proAuthoriseCache = this.redisLogic.getProAuthoriseCache(requestDto.getCode());
    if (proAuthoriseCache == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidCode);
    }

    // verify the client_id in the request matches the one the code was issued to — prevents a client from redeeming another client's code.
    if (!credentialsDto.getId().equals(proAuthoriseCache.getPreAuthoriseCache().getClientId())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidClientId);
    }

    // PKCE: verify code_challenge was present in the original authorisation request — rejects clients that skipped PKCE.
    if (proAuthoriseCache.getPreAuthoriseCache().getCodeChallenge() == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidCodeChallenge);
    }

    // PKCE: verify the code_challenge_method is SHA256 — the only supported method.
    String codeChallengeMethodInCache = proAuthoriseCache.getPreAuthoriseCache().getCodeChallengeMethod();
    if (!codeChallengeMethodInCache.equals(codeChallengeMethod)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidCodeChallengeMethod);
    }

    // PKCE: recreate code_challenge from the incoming code_verifier, and verify it matches the cached code_challenge
    // — proves the token request comes from the same client that initiated the authorisation request.
    MessageDigest md = DigestUtils.getSha256Digest();
    md.update(requestDto.getCodeVerifier().getBytes());
    String codeChallenge = Base64URL.encode(md.digest()).toString();
    if (!proAuthoriseCache.getPreAuthoriseCache().getCodeChallenge().equals(codeChallenge)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidCodeChallenge);
    }

    return new TokenValidationResultDto(credentialsDto.getId(), proAuthoriseCache, requestDto.getCode());
  }

  /**
   * Generates tokens.
   *
   * @param tokenValidationResultDto
   * @return GenerateTokensResponseDto
   */
  @Override
  @Transactional
  public GenerateTokensResponseDto execute(
    TokenValidationResultDto<ProAuthoriseCache> tokenValidationResultDto
  ) throws Exception {

    Usr usr = this.usrLogic.getUsrBySub(tokenValidationResultDto.getPayload().getSub());
    if(usr == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorNoMatchingUser);
    }

    GenerateTokensResponseDto responseDto = this.tokenLogic.generateTokensWithRefreshToken(
      tokenValidationResultDto.getClientId(),
      usr.getSub(),
      tokenValidationResultDto.getPayload().getScopeRequested(),
      tokenValidationResultDto.getPayload().getPreAuthoriseCache().getNonce()
    );

    // RFC 6749 §4.1.2: auth codes must be single-use — delete immediately after a successful token exchange
    this.redisLogic.deleteProAuthoriseCache(tokenValidationResultDto.getCode());

    return responseDto;
  }
}
