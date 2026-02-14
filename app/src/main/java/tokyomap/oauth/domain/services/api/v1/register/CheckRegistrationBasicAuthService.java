package tokyomap.oauth.domain.services.api.v1.register;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tokyomap.oauth.domain.services.api.v1.ApiException;

@Service
public class CheckRegistrationBasicAuthService {

  private final String adminUsername;
  private final String adminPassword;

  public CheckRegistrationBasicAuthService(
      @Value("${registration.admin.username}") String adminUsername,
      @Value("${registration.admin.password}") String adminPassword
  ) {
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  /**
   * Validates the Basic Auth header on the client registration endpoint.
   * Only the admin console is authorised to register clients.
   *
   * @param authorization the value of the Authorization header
   * @throws ApiException if the header is missing, malformed, or credentials are invalid
   */
  public void execute(String authorization) throws ApiException {

    if (authorization == null || !authorization.toLowerCase().startsWith("basic ")) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorised");
    }

    String encoded = authorization.substring("basic ".length());
    String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorised");
    }

    int colonIndex = decoded.indexOf(':');
    if (colonIndex < 0) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorised");
    }

    String username = decoded.substring(0, colonIndex);
    String password = decoded.substring(colonIndex + 1);

    if (!username.equals(this.adminUsername) || !password.equals(this.adminPassword)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorised");
    }
  }
}
