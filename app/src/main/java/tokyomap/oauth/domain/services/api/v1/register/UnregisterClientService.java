package tokyomap.oauth.domain.services.api.v1.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.TokenLogic;

@Service
public class UnregisterClientService {

  private static final String TOKEN_TYPE_HINT_ACCESS_TOKEN = "access_token";
  private static final String TOKEN_TYPE_HINT_REFRESH_TOKEN = "refresh_token";

  private final TokenLogic tokenLogic;
  private final ClientLogic clientLogic;

  @Autowired
  public UnregisterClientService(TokenLogic tokenLogic, ClientLogic clientLogic) {
    this.tokenLogic = tokenLogic;
    this.clientLogic = clientLogic;
  }

  /**
   * Unregisters the client and its tokens fot the given clientId.
   *
   * @param clientId
   * @param accessToken
   * @param refreshToken
   */
  @Transactional
  public void execute(String clientId, String accessToken, String refreshToken) {
    this.clientLogic.unregisterClient(clientId);
    this.tokenLogic.revokeToken(accessToken, TOKEN_TYPE_HINT_ACCESS_TOKEN);
    this.tokenLogic.revokeToken(accessToken, TOKEN_TYPE_HINT_REFRESH_TOKEN);
  }
}
