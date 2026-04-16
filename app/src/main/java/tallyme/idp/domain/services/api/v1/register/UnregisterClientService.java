package tallyme.idp.domain.services.api.v1.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tallyme.idp.domain.logics.ClientLogic;
import tallyme.idp.domain.logics.TokenLogic;

@Service
public class UnregisterClientService {

  private final TokenLogic tokenLogic;
  private final ClientLogic clientLogic;

  private final String tokenTypeHintAccessToken;
  private final String tokenTypeHintRefreshToken;

  @Autowired
  public UnregisterClientService(
    TokenLogic tokenLogic,
    ClientLogic clientLogic,
    @Value("${oauth.token.type.hint.access-token}") String tokenTypeHintAccessToken,
    @Value("${oauth.token.type.hint.refresh-token}") String tokenTypeHintRefreshToken
  ) {
    this.tokenLogic = tokenLogic;
    this.clientLogic = clientLogic;
    this.tokenTypeHintAccessToken = tokenTypeHintAccessToken;
    this.tokenTypeHintRefreshToken = tokenTypeHintRefreshToken;
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
    this.tokenLogic.revokeToken(accessToken, tokenTypeHintAccessToken);
    this.tokenLogic.revokeToken(refreshToken, tokenTypeHintRefreshToken);
  }
}
