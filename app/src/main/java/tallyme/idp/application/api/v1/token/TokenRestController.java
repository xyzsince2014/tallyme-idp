package tallyme.idp.application.api.v1.token;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tallyme.idp.domain.entities.redis.ProAuthoriseCache;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.domain.services.api.v1.token.AuthorisationCodeFlowService;
import tallyme.idp.domain.services.api.v1.token.ClientCredentialsSerivce;
import tallyme.idp.domain.services.api.v1.token.RefreshTokenService;
import tallyme.idp.dtos.CredentialsDto;
import tallyme.idp.dtos.GenerateTokensRequestDto;
import tallyme.idp.dtos.GenerateTokensResponseDto;
import tallyme.idp.dtos.TokenValidationResultDto;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/token")
public class TokenRestController {

  private static final String GRANT_TYPE_AUTHORISATION_CODE = "authorization_code";
  private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

  private static final String ERROR_MESSAGE_INVALID_GRANT_TYPE = "Invalid Grant Type";

  private final AuthorisationCodeFlowService authorisationCodeFlowService;
  private final RefreshTokenService refreshTokenService;
  private final ClientCredentialsSerivce clientCredentialsSerivce;
  private final String errorGrantTypeRequired;
  private final String errorCodeRequired;
  private final String errorCodeVerifierRequired;
  private final String errorRefreshTokenRequired;

  @Autowired
  public TokenRestController(
      AuthorisationCodeFlowService authorisationCodeFlowService,
      RefreshTokenService refreshTokenService,
      ClientCredentialsSerivce clientCredentialsSerivce,
      @Value("${error.grant-type-required}") String errorGrantTypeRequired,
      @Value("${error.code-required}") String errorCodeRequired,
      @Value("${error.code-verifier-required}") String errorCodeVerifierRequired,
      @Value("${error.refresh-token-required}") String errorRefreshTokenRequired
  ) {
    this.authorisationCodeFlowService = authorisationCodeFlowService;
    this.refreshTokenService = refreshTokenService;
    this.clientCredentialsSerivce = clientCredentialsSerivce;
    this.errorGrantTypeRequired = errorGrantTypeRequired;
    this.errorCodeRequired = errorCodeRequired;
    this.errorCodeVerifierRequired = errorCodeVerifierRequired;
    this.errorRefreshTokenRequired = errorRefreshTokenRequired;
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
      requestDto.setScope(params.get("scope"));

      String grantType = requestDto.getGrantType();
      if (grantType == null || grantType.isEmpty()) {
        throw new ApiException(HttpStatus.BAD_REQUEST, this.errorGrantTypeRequired);
      }

      switch (grantType) {
        case GRANT_TYPE_AUTHORISATION_CODE: {
          if (requestDto.getCode() == null || requestDto.getCode().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, this.errorCodeRequired);
          }
          if (requestDto.getCodeVerifier() == null || requestDto.getCodeVerifier().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, this.errorCodeVerifierRequired);
          }
          TokenValidationResultDto<ProAuthoriseCache> tokenValidationResultDto =
            this.authorisationCodeFlowService.execValidation(requestDto, authorization);
          GenerateTokensResponseDto responseDto = this.authorisationCodeFlowService.execute(tokenValidationResultDto);
          return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        }
        case GRANT_TYPE_REFRESH_TOKEN: {
          if (requestDto.getRefreshToken() == null || requestDto.getRefreshToken().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, this.errorRefreshTokenRequired);
          }
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
