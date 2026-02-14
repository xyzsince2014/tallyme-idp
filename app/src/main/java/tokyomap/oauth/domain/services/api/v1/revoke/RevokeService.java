package tokyomap.oauth.domain.services.api.v1.revoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.TokenLogic;
import tokyomap.oauth.domain.services.api.v1.ApiException;
import tokyomap.oauth.domain.services.api.v1.TokenScrutinyService;
import tokyomap.oauth.dtos.CredentialsDto;
import tokyomap.oauth.dtos.RevokeRequestDto;
import tokyomap.oauth.utils.Decorder;

@Service
public class RevokeService {

  // todo: use global constants
  private static final String ERROR_MESSAGE_INVALID_CLIENT = "Invalid Client";
  private static final String ERROR_MESSAGE_NO_AUTHORIZATION_HEADER = "No Authorization Header";

  private final TokenScrutinyService tokenScrutinyService;
  private final ClientLogic clientLogic;
  private final TokenLogic tokenLogic;
  private final Decorder decorder;

  @Autowired
  public RevokeService(TokenScrutinyService tokenScrutinyService, ClientLogic clientLogic, TokenLogic tokenLogic, Decorder decorder) {
    this.tokenScrutinyService = tokenScrutinyService;
    this.clientLogic = clientLogic;
    this.tokenLogic = tokenLogic;
    this.decorder = decorder;
  }

  /**
   * Revokes the given access and refresh tokens.
   *
   * @param requestDto
   * @param authorization
   */
  @Transactional
  public void execute(RevokeRequestDto requestDto, String authorization) throws Exception {

    CredentialsDto credentialsDto = this.decorder.decodeCredentials(authorization);
    if (credentialsDto == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE_NO_AUTHORIZATION_HEADER);
    }

    Client client = this.clientLogic.getClientByClientId(credentialsDto.getId());
    if (client == null || !client.getClientSecret().equals(credentialsDto.getSecret())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, ERROR_MESSAGE_INVALID_CLIENT);
    }

    this.tokenLogic.revokeToken(requestDto.getToken(), requestDto.getTokenTypeHint());
  }
}
