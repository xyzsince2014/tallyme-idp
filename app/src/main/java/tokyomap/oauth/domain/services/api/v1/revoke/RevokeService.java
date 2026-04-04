package tokyomap.oauth.domain.services.api.v1.revoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  private final ClientLogic clientLogic;
  private final TokenLogic tokenLogic;
  private final Decorder decorder;

  private final String errorInvalidClient;
  private final String errorNoAuthorizationHeader;

  @Autowired
  public RevokeService(
    TokenScrutinyService tokenScrutinyService,
    ClientLogic clientLogic,
    TokenLogic tokenLogic,
    Decorder decorder,
    @Value("${error.invalid-client}") String errorInvalidClient,
    @Value("${error.no-authorization-header}") String errorNoAuthorizationHeader
  ) {
    this.clientLogic = clientLogic;
    this.tokenLogic = tokenLogic;
    this.decorder = decorder;
    this.errorInvalidClient = errorInvalidClient;
    this.errorNoAuthorizationHeader = errorNoAuthorizationHeader;
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
      throw new ApiException(HttpStatus.UNAUTHORIZED, errorNoAuthorizationHeader);
    }

    Client client = this.clientLogic.getClientByClientId(credentialsDto.getId());
    if (client == null || !client.getClientSecret().equals(credentialsDto.getSecret())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, errorInvalidClient);
    }

    this.tokenLogic.revokeToken(requestDto.getToken(), requestDto.getTokenTypeHint());
  }
}
