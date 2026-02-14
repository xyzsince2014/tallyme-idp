package tokyomap.oauth.domain.services.api.v1.token;

import com.nimbusds.jose.util.Base64URL;
import java.security.MessageDigest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.entities.postgres.Usr;
import tokyomap.oauth.domain.entities.redis.ProAuthoriseCache;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.RedisLogic;
import tokyomap.oauth.domain.logics.TokenLogic;
import tokyomap.oauth.domain.logics.UsrLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.GenerateTokensRequestDto;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;
import tokyomap.oauth.dtos.TokenValidationResultDto;
import tokyomap.oauth.utils.Decorder;

@Service
public class AuthorisationCodeFlowService extends TokenService<ProAuthoriseCache> {

  // todo: use global constants
  private static final String CODE_CHALLENGE_METHOD = "S256"; // RFC 7636

  private static final String ERROR_MESSAGE_INVALID_CODE = "Invalid Authorisation Code";
  private static final String ERROR_MESSAGE_INVALID_CLIENT_ID = "Invalid Client Id";
  private static final String ERROR_MESSAGE_INVALID_CODE_CHALLENGE = "Invalid Code Challenge";
  private static final String ERROR_MESSAGE_INVALID_CODE_CHALLENGE_METHOD = "Invalid Code Challenge Method";
  private static final String ERROR_MESSAGE_NO_MATCHING_USER = "No Matching User";

  private final RedisLogic redisLogic;
  private final TokenLogic tokenLogic;
  private final UsrLogic usrLogic;

  @Autowired
  public AuthorisationCodeFlowService(
    ClientLogic clientLogic, Decorder decorder, RedisLogic redisLogic, TokenLogic tokenLogic, UsrLogic usrLogic

  ) {
    super(clientLogic, decorder);
    this.redisLogic = redisLogic;
    this.tokenLogic = tokenLogic;
    this.usrLogic = usrLogic;
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
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_CODE);
    }

    // verify the client_id in the request matches the one the code was issued to — prevents a client from redeeming another client's code.
    if (!credentialsDto.getId().equals(proAuthoriseCache.getPreAuthoriseCache().getClientId())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_CLIENT_ID);
    }

    // PKCE: verify code_challenge was present in the original authorisation request — rejects clients that skipped PKCE.
    if (proAuthoriseCache.getPreAuthoriseCache().getCodeChallenge() == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_CODE_CHALLENGE);
    }

    // PKCE: verify the code_challenge_method is SHA256 — the only supported method.
    String codeChallengeMethod = proAuthoriseCache.getPreAuthoriseCache().getCodeChallengeMethod();
    if (!codeChallengeMethod.equals(CODE_CHALLENGE_METHOD)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_CODE_CHALLENGE_METHOD);
    }

    // PKCE: recreate code_challenge from the incoming code_verifier, and verify it matches the cached code_challenge
    // — proves the token request comes from the same client that initiated the authorisation request.
    MessageDigest md = DigestUtils.getSha256Digest();
    md.update(requestDto.getCodeVerifier().getBytes());
    String codeChallenge = Base64URL.encode(md.digest()).toString();
    if (!proAuthoriseCache.getPreAuthoriseCache().getCodeChallenge().equals(codeChallenge)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_INVALID_CODE_CHALLENGE);
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
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_NO_MATCHING_USER);
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
