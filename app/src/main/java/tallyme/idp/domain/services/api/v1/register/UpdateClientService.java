package tallyme.idp.domain.services.api.v1.register;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tallyme.idp.domain.entities.postgres.Client;
import tallyme.idp.domain.logics.ClientLogic;
import tallyme.idp.domain.services.api.v1.ApiException;
import tallyme.idp.dtos.ClientValidationResultDto;
import tallyme.idp.dtos.RequestClientDto;
import tallyme.idp.dtos.ResponseClientDto;

@Service
public class UpdateClientService extends RegisterService {

  private final ClientLogic clientLogic;

  @Autowired
  public UpdateClientService(
    ClientLogic clientLogic,
    @Value("${oauth.response.type.code}") String responseTypeCode,
    @Value("#{'${oauth.response.types}'.split(',')}") String[] responseTypes,
    @Value("${oauth.grant.type.authorization-code}") String grantTypeAuthorizationCode,
    @Value("#{'${oauth.grant.types}'.split(',')}") String[] grantTypes,
    @Value("${oauth.token-endpoint.auth.method.default}") String tokenEndpointAuthMethodDefault,
    @Value("#{'${oauth.token-endpoint.auth.methods}'.split(',')}") String[] tokenEndpointAuthMethods,
    @Value("${REGISTRATION_ENDPOINT}") String registrationEndpoint,
    @Value("${error.invalid-client-name}") String errorInvalidClientName,
    @Value("${error.invalid-client-uri}") String errorInvalidClientUri,
    @Value("${error.invalid-redirect-uris}") String errorInvalidRedirectUris,
    @Value("${error.invalid-scope}") String errorInvalidScope,
    @Value("${error.invalid-token-endpoint-auth-method}") String errorInvalidTokenEndpointAuthMethod,
    @Value("${error.invalid-grant-types}") String errorInvalidGrantTypes,
    @Value("${error.invalid-response-types}") String errorInvalidResponseTypes
  ) {
    super(responseTypeCode, responseTypes, grantTypeAuthorizationCode, grantTypes,
        tokenEndpointAuthMethodDefault, tokenEndpointAuthMethods, registrationEndpoint,
        errorInvalidClientName, errorInvalidClientUri, errorInvalidRedirectUris, errorInvalidScope,
        errorInvalidTokenEndpointAuthMethod, errorInvalidGrantTypes, errorInvalidResponseTypes);
    this.clientLogic = clientLogic;
  }

  /**
   * Overrides execValidation() to only validate fields relevant to an update.
   * client_uri, redirect_uris and scope are intentionally excluded
   *   — the update flow keeps those existing values from the registered client and does not update them.
   *
   * @param requestClientDto
   * @return ClientValidationResultDto
   * @throws ApiException
   */
  @Override
  public ClientValidationResultDto execValidation(RequestClientDto requestClientDto) throws ApiException {
    if (requestClientDto.getClientName() == null || requestClientDto.getClientName().trim().isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, errorInvalidClientName);
    }

    String tokenEndpointAuthMethod = Arrays.stream(tokenEndpointAuthMethods)
        .filter(m -> m.equals(requestClientDto.getTokenEndpointAuthMethod() == null ? tokenEndpointAuthMethodDefault : requestClientDto.getTokenEndpointAuthMethod()))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, errorInvalidTokenEndpointAuthMethod));

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
        String.join(" ", responseClientDto.getScope()),
        RandomStringUtils.random(8, true, true),
        responseClientDto.getRegistrationClientUri(),
        now.plusDays(90),
        responseClientDto.getCreatedAt(),
        now
    );
  }
}
