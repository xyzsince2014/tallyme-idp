package tokyomap.oauth.application.api.v1.token;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokyomap.oauth.domain.entities.redis.ProAuthoriseCache;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.domain.services.api.v1.token.AuthorisationCodeFlowService;
import tokyomap.oauth.domain.services.api.v1.token.ClientCredentialsSerivce;
import tokyomap.oauth.domain.services.api.v1.token.RefreshTokenService;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.GenerateTokensRequestDto;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;
import tokyomap.oauth.dtos.TokenValidationResultDto;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/token")
public class TokenRestController {

  // todo: use global constants
  private static final String GRANT_TYPE_AUTHORISATION_CODE = "authorization_code";
  private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  private static final String ERROR_MESSAGE_INVALID_GRANT_TYPE = "Invalid Grant Type";

  private final AuthorisationCodeFlowService authorisationCodeFlowService;
  private final RefreshTokenService refreshTokenService;
  private final ClientCredentialsSerivce clientCredentialsSerivce;

  @Autowired
  public TokenRestController(
      AuthorisationCodeFlowService authorisationCodeFlowService,
      RefreshTokenService refreshTokenService,
      ClientCredentialsSerivce clientCredentialsSerivce
  ) {
    this.authorisationCodeFlowService = authorisationCodeFlowService;
    this.refreshTokenService = refreshTokenService;
    this.clientCredentialsSerivce = clientCredentialsSerivce;
  }

  @RequestMapping(method = RequestMethod.POST, headers = "Content-Type=application/x-www-form-urlencoded;charset=utf-8")
  public ResponseEntity<GenerateTokensResponseDto> generateTokens(
    @RequestParam Map<String, String> params,
    @RequestHeader("Authorization") String authorization
  ) {
    try {
      GenerateTokensRequestDto requestDto = new GenerateTokensRequestDto();
      requestDto.setGrantType(params.get("grant_type"));
      requestDto.setCode(params.get("code"));
      requestDto.setRedirectUri(params.get("redirect_uri"));
      requestDto.setCodeVerifier(params.get("code_verifier"));
      requestDto.setRefreshToken(params.get("refresh_token"));
      requestDto.setClientId(params.get("client_id"));
      requestDto.setClientSecret(params.get("client_secret"));

      String scopeStr = params.get("scope");
      String[] scope = (scopeStr != null && !scopeStr.isEmpty()) ? scopeStr.split(" ") : new String[0];
      requestDto.setScope(scope);

      switch (requestDto.getGrantType()) {
        case GRANT_TYPE_AUTHORISATION_CODE: {
          TokenValidationResultDto<ProAuthoriseCache> tokenValidationResultDto =
            this.authorisationCodeFlowService.execValidation(requestDto, authorization);
          GenerateTokensResponseDto responseDto = this.authorisationCodeFlowService.execute(tokenValidationResultDto);
          return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        }
        case GRANT_TYPE_REFRESH_TOKEN: {
          TokenValidationResultDto<SignedJWT> tokenValidationResultDto =
            this.refreshTokenService.execValidation(requestDto, authorization);
          GenerateTokensResponseDto responseDto = this.refreshTokenService.execute(tokenValidationResultDto);
          return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        }
        case GRANT_TYPE_CLIENT_CREDENTIALS: {
          TokenValidationResultDto<CredentialsDto> tokenValidationResultDto =
            this.clientCredentialsSerivce.execValidation(requestDto, authorization);
          GenerateTokensResponseDto responseDto = this.clientCredentialsSerivce.execute(tokenValidationResultDto);
          return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        }
        default: {
          GenerateTokensResponseDto responseDto = new GenerateTokensResponseDto(ERROR_MESSAGE_INVALID_GRANT_TYPE);
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        }
      }

    } catch (ApiException e) {
      GenerateTokensResponseDto responseDto = new GenerateTokensResponseDto(e.getErrorMessage());
      return ResponseEntity.status(e.getStatusCode()).body(responseDto);

    } catch (Exception e) {
      return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
