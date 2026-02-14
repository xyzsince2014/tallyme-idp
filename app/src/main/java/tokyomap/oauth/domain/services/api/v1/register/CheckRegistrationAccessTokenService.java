package tokyomap.oauth.domain.services.api.v1.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;

@Service
public class CheckRegistrationAccessTokenService {

  private final ClientLogic clientLogic;

  @Autowired
  public CheckRegistrationAccessTokenService(ClientLogic clientLogic) {
    this.clientLogic = clientLogic;
  }

  /**
   * Makes sure that the client referred to is the same one that the registration access token was issued to.
   *
   * @param clientId
   * @param authorization
   * @return registered client
   * @throws ApiException
   */
  public Client execute(String clientId, String authorization) throws ApiException {

    Client client = this.clientLogic.getClientByClientId(clientId);
    if (client == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Client Not Found");
    }

    if (authorization == null || authorization.toLowerCase().indexOf("basic") == -1) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorised");
    }

    String token = authorization.substring("bearer ".length());
    if (!token.equals(client.getRegistrationAccessToken())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Access Token");
    }

    return client;
  }
}
