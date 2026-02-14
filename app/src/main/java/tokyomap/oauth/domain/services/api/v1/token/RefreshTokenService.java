package tokyomap.oauth.domain.services.api.v1.token;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
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

  // todo: use global constants
  private static final String TOKEN_TYPE_HINT_REFRESH_TOKEN = "refresh_token";

  private static final String ERROR_MESSAGE_NO_MATCHING_REFRESH_TOKEN = "No Matching RefreshToken";
  private static final String ERROR_MESSAGE_NO_MATCHING_USER = "No Matching User";

  private final TokenScrutinyService tokenScrutinyService;
  private final TokenLogic tokenLogic;
  private final UsrLogic usrLogic;

  @Autowired
  public RefreshTokenService(
    TokenScrutinyService tokenScrutinyService, ClientLogic clientLogic, Decorder decorder, TokenLogic tokenLogic, UsrLogic usrLogic
  ) {
    super(clientLogic, decorder);
    this.tokenScrutinyService = tokenScrutinyService;
    this.tokenLogic = tokenLogic;
    this.usrLogic = usrLogic;
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
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_NO_MATCHING_REFRESH_TOKEN);
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
    this.tokenLogic.revokeToken(refreshToken, TOKEN_TYPE_HINT_REFRESH_TOKEN);

    Usr usr = this.usrLogic.getUsrBySub(tokenValidationResultDto.getPayload().getJWTClaimsSet().getSubject());
    if(usr == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, ERROR_MESSAGE_NO_MATCHING_USER);
    }

    String clientId = tokenValidationResultDto.getPayload().getJWTClaimsSet().getStringClaim("clientId");
    String[] scopes = tokenValidationResultDto.getPayload().getJWTClaimsSet().getStringArrayClaim("scopes");
    GenerateTokensResponseDto responseDto =
      this.tokenLogic.generateTokensWithRefreshToken(clientId, usr.getSub(), scopes, null);

    return responseDto;
  }
}
