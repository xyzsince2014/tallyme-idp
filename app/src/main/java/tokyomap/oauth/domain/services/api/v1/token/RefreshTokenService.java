package tokyomap.oauth.domain.services.api.v1.token;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.entities.postgres.RefreshToken;
import tokyomap.oauth.domain.entities.postgres.Usr;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.TokenLogic;
import tokyomap.oauth.domain.logics.UsrLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.domain.services.api.v1.TokenScrutinyService;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.GenerateTokensRequestDto;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;
import tokyomap.oauth.dtos.TokenValidationResultDto;
import tokyomap.oauth.utils.Decorder;

@Service
public class RefreshTokenService extends TokenService<SignedJWT> {

  private final TokenScrutinyService tokenScrutinyService;
  private final TokenLogic tokenLogic;
  private final UsrLogic usrLogic;

  private final String tokenTypeHintRefreshToken;
  private final String errorNoMatchingRefreshToken;
  private final String errorNoMatchingUser;

  @Autowired
  public RefreshTokenService(
    TokenScrutinyService tokenScrutinyService,
    ClientLogic clientLogic,
    Decorder decorder,
    TokenLogic tokenLogic,
    UsrLogic usrLogic,
    @Value("${error.invalid-client-id}") String errorInvalidClientId,
    @Value("${error.no-matching-client}") String errorNoMatchingClient,
    @Value("${error.no-matching-client-secret}") String errorNoMatchingClientSecret,
    @Value("${oauth.token.type.hint.refresh-token}") String tokenTypeHintRefreshToken,
    @Value("${error.no-matching-refresh-token}") String errorNoMatchingRefreshToken,
    @Value("${error.no-matching-user}") String errorNoMatchingUser
  ) {
    super(clientLogic, decorder, errorInvalidClientId, errorNoMatchingClient, errorNoMatchingClientSecret);
    this.tokenScrutinyService = tokenScrutinyService;
    this.tokenLogic = tokenLogic;
    this.usrLogic = usrLogic;
    this.tokenTypeHintRefreshToken = tokenTypeHintRefreshToken;
    this.errorNoMatchingRefreshToken = errorNoMatchingRefreshToken;
    this.errorNoMatchingUser = errorNoMatchingUser;
  }

  /**
   * Validates requests to the token endpoint with refresh token.
   *
   * @return TokenValidationResultDto
   */
  @Override
  public TokenValidationResultDto<SignedJWT> execValidation(GenerateTokensRequestDto requestDto, String authorization) throws ApiException {

    CredentialsDto credentialsDto = this.validateClient(requestDto, authorization);
    String incomingToken = requestDto.getRefreshToken();

    SignedJWT refreshJWT = this.tokenScrutinyService.execute(credentialsDto, incomingToken);

    RefreshToken refreshToken = this.tokenLogic.getRefreshToken(incomingToken);
    if(refreshToken == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorNoMatchingRefreshToken);
    }

    return new TokenValidationResultDto(credentialsDto.getId(), refreshJWT);
  }

  /**
   * Generates tokens to refresh old ones.
   *
   * @param tokenValidationResultDto
   * @return GenerateTokensResponseDto
   */
  @Override
  @Transactional
  public GenerateTokensResponseDto execute(TokenValidationResultDto<SignedJWT> tokenValidationResultDto) throws Exception {

    // revoke the old refresh token before generate the new one
    String refreshToken = tokenValidationResultDto.getPayload().serialize();
    this.tokenLogic.revokeToken(refreshToken, tokenTypeHintRefreshToken);

    Usr usr = this.usrLogic.getUsrBySub(tokenValidationResultDto.getPayload().getJWTClaimsSet().getSubject());
    if(usr == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorNoMatchingUser);
    }

    String clientId = tokenValidationResultDto.getPayload().getJWTClaimsSet().getStringClaim("clientId");
    String scope = tokenValidationResultDto.getPayload().getJWTClaimsSet().getStringClaim("scope");

    GenerateTokensResponseDto responseDto =
      this.tokenLogic.generateTokensWithRefreshToken(clientId, usr.getSub(), scope, null);

    return responseDto;
  }
}
