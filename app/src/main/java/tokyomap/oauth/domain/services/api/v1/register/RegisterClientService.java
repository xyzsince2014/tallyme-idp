package tokyomap.oauth.domain.services.api.v1.register;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.dtos.ClientValidationResultDto;
import tokyomap.oauth.dtos.RequestClientDto;

@Service
public class RegisterClientService extends RegisterService {

  // todo: use global constants
  private static final int CLIENT_LIFETIME = 90; // days

  private final ClientLogic clientLogic;

  @Autowired
  public RegisterClientService(ClientLogic clientLogic) {
    this.clientLogic = clientLogic;
  }

  /**
   * register the given client
   * @param requestClientDto
   * @param validationResultDto
   * @return clientRegistered
   */
  @Transactional
  public Client execute(RequestClientDto requestClientDto, ClientValidationResultDto validationResultDto) throws Exception {
    String clientId = RandomStringUtils.random(8, true, true);
    String clientSecret = this.generateClientSecret(validationResultDto.getTokenEndpointAuthMethod());

    String registrationAccessToken = RandomStringUtils.random(8, true, true);
    String registrationClientUri = this.buildRegistrationClientUri(clientId);

    Client client = this.buildClient(
      requestClientDto,
      validationResultDto,
      clientId,
      clientSecret,
      registrationAccessToken,
      registrationClientUri
    );

    return this.clientLogic.registerClient(client);
  }

  /**
   * Generates a client secret if the token endpoint auth method requires one, i.e. all methods except NONE.
   *
   * @param tokenEndpointAuthMethod
   * @return clientSecret, or null if the auth method is NONE
   */
  private String generateClientSecret(String tokenEndpointAuthMethod) {
    return Arrays.stream(TOKEN_ENDPOINT_AUTH_METHODS).anyMatch(m -> m.equals(tokenEndpointAuthMethod))
        ? RandomStringUtils.random(8, true, true)
        : null;
  }

  /**
   * Builds the client configuration endpoint URI by concatenating the client ID to the registration endpoint.
   * RFC 7591 §3.2: the registration client URI is the URI of the client configuration endpoint.
   *
   * @param clientId
   * @return registrationClientUri
   */
  private String buildRegistrationClientUri(String clientId) {
    return REGISTRATION_ENDPOINT + "/" + clientId;
  }

  /**
   * Builds the Client entity by merging the validated values from validationResultDto with the remaining fields from requestClientDto.
   *
   * @param requestClientDto
   * @param validationResultDto
   * @param clientId
   * @param clientSecret
   * @param registrationAccessToken
   * @param registrationClientUri
   * @return Client
   */
  private Client buildClient(
      RequestClientDto requestClientDto,
      ClientValidationResultDto validationResultDto,
      String clientId,
      String clientSecret,
      String registrationAccessToken,
      String registrationClientUri
  ) {
    LocalDateTime now = LocalDateTime.now();
    return new Client(
        clientId,
        clientSecret,
        requestClientDto.getClientName(),
        validationResultDto.getTokenEndpointAuthMethod(),
        requestClientDto.getClientUri(),
        String.join(" ", requestClientDto.getRedirectUris()),
        String.join(" ", validationResultDto.getGrantTypes()),
        String.join(" ", validationResultDto.getResponseTypes()),
        String.join(" ", requestClientDto.getScope()),
        registrationAccessToken,
        registrationClientUri,
        now.plusDays(CLIENT_LIFETIME),
        now,
        now
    );
  }
}
