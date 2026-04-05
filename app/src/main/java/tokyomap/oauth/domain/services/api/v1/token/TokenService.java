package tokyomap.oauth.domain.services.api.v1.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.GenerateTokensRequestDto;
import tokyomap.oauth.dtos.GenerateTokensResponseDto;
import tokyomap.oauth.dtos.TokenValidationResultDto;
import tokyomap.oauth.utils.Decorder;

public abstract class TokenService<T> {

  private final ClientLogic clientLogic;
  private final Decorder decorder;

  protected final String errorInvalidClientId;
  private final String errorNoMatchingClient;
  private final String errorNoMatchingClientSecret;

  public TokenService(
    ClientLogic clientLogic,
    Decorder decorder,
    @Value("${error.invalid-client-id}") String errorInvalidClientId,
    @Value("${error.no-matching-client}") String errorNoMatchingClient,
    @Value("${error.no-matching-client-secret}") String errorNoMatchingClientSecret
  ) {
    this.clientLogic = clientLogic;
    this.decorder = decorder;
    this.errorInvalidClientId = errorInvalidClientId;
    this.errorNoMatchingClient = errorNoMatchingClient;
    this.errorNoMatchingClientSecret = errorNoMatchingClientSecret;
  }

  /**
   * execute validation of request to the token endpoint
   * @return TokenValidationResultDto
   */
  public abstract TokenValidationResultDto<T> execValidation(GenerateTokensRequestDto requestDto, String authorization) throws ApiException;

  /**
   * generate tokens
   * @param tokenValidationResultDto
   * @return GenerateTokensResponseDto
   */
  public abstract GenerateTokensResponseDto execute(TokenValidationResultDto<T> tokenValidationResultDto) throws Exception;

  /**
   * Validates the client.
   *
   * @param requestDto
   * @param authorization
   * @return CredentialsDto
   */
  protected CredentialsDto validateClient(
    GenerateTokensRequestDto requestDto, String authorization
  ) throws ApiException {

    String clientId = "";
    String clientSecret = "";

    // fetch clientId & clientSecret from the authorization header or the post params, then check them.
    CredentialsDto credentialsDto = this.decorder.decodeCredentials(authorization);

    clientId = credentialsDto.getId();
    clientSecret = credentialsDto.getSecret();

    if (requestDto.getClientId() != null) {
      if (credentialsDto.getId() != null) {
        // return an error if we've already seen the client's credentials in the authorization header
        throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidClientId);
      }
      clientId = requestDto.getClientId();
      clientSecret = requestDto.getClientSecret();
    }

    Client client = this.clientLogic.getClientByClientId(clientId);
    if (client == null) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorNoMatchingClient);
    }
    if (!client.getClientSecret().equals(clientSecret)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorNoMatchingClientSecret);
    }

    return new CredentialsDto(clientId, clientSecret, client.getScope());
  }
}
