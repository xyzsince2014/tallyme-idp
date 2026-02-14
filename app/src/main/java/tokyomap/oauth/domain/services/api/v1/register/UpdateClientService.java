package tokyomap.oauth.domain.services.api.v1.register;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.dtos.ClientValidationResultDto;
import tokyomap.oauth.dtos.RequestClientDto;
import tokyomap.oauth.dtos.ResponseClientDto;

@Service
public class UpdateClientService extends RegisterService {

  // todo: use global constants
  private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_BASIC = "client_secret_basic";

  private final ClientLogic clientLogic;

  @Autowired
  public UpdateClientService(ClientLogic clientLogic) {
    this.clientLogic = clientLogic;
  }

  /**
   * Overrides execValidation() to only validate fields relevant to an update.
   * client_uri, redirect_uris and scopes are intentionally excluded
   *   — the update flow keeps those existing values from the registered client and does not update them.
   *
   * @param requestClientDto
   * @return ClientValidationResultDto
   * @throws ApiException
   */
  @Override
  public ClientValidationResultDto execValidation(RequestClientDto requestClientDto) throws ApiException {
    if (requestClientDto.getClientName() == null || requestClientDto.getClientName().trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Client Name.");
    }

    String tokenEndpointAuthMethod = Arrays.stream(TOKEN_ENDPOINT_AUTH_METHODS)
        .filter(m -> m.equals(requestClientDto.getTokenEndpointAuthMethod() == null ? TOKEN_ENDPOINT_AUTH_METHOD_CLIENT_SECRET_BASIC : requestClientDto.getTokenEndpointAuthMethod()))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid tokenEndpointAuthMethod."));

    return this.validateAndResolveGrantAndResponseTypes(requestClientDto, tokenEndpointAuthMethod);
  }

  /**
   * Executes additional validation.
   *
   * @param requestClientDto
   * @param responseClientDto
   * @return clientNameToUpdate
   * @throws ApiException
   */
  public String execAdditionalValidation(RequestClientDto requestClientDto, ResponseClientDto responseClientDto) throws ApiException {
    if (!requestClientDto.getClientId().equals(responseClientDto.getClientId())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid ClientId");
    }
    if (requestClientDto.getClientSecret() != null && !requestClientDto.getClientSecret().equals(responseClientDto.getClientSecret())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Client Secret");
    }
    return requestClientDto.getClientName();
  }

  /**
   * Updates the registered client.
   *
   * @param clientNameToUpdate
   * @param validationResultDto
   * @return clientUpdated
   * @throws Exception
   */
  public Client execute(
    String clientNameToUpdate, ClientValidationResultDto validationResultDto, ResponseClientDto responseClientDto
  ) throws Exception {
    Client clientToBeUpdated = this.buildClient(clientNameToUpdate, validationResultDto, responseClientDto);
    return this.clientLogic.registerClient(clientToBeUpdated);
  }

  /**
   * Builds the Client entity by merging the validated values from validationResultDto
   * with the immutable fields kept from the registered client via responseClientDto.
   *
   * @param clientNameToUpdate
   * @param validationResultDto
   * @param responseClientDto
   * @return Client
   */
  private Client buildClient(
    String clientNameToUpdate, ClientValidationResultDto validationResultDto, ResponseClientDto responseClientDto
  ) {
    LocalDateTime now = LocalDateTime.now();
    return new Client(
        responseClientDto.getClientId(),
        responseClientDto.getClientSecret(),
        clientNameToUpdate,
        validationResultDto.getTokenEndpointAuthMethod(),
        responseClientDto.getClientUri(),
        String.join(" ", responseClientDto.getRedirectUris()),
        String.join(" ", validationResultDto.getGrantTypes()),
        String.join(" ", validationResultDto.getResponseTypes()),
        String.join(" ", responseClientDto.getScopes()),
        RandomStringUtils.random(8, true, true),
        responseClientDto.getRegistrationClientUri(),
        now.plusDays(90),
        responseClientDto.getCreatedAt(),
        now
    );
  }
}
