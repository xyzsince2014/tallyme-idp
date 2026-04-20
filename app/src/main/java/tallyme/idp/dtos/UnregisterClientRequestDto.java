package tallyme.idp.dtos;

import java.io.Serializable;

public class UnregisterClientRequestDto implements Serializable {

  private static final long serialVersionUID = -596832360206019466L;

  private String accessToken;

  private String refreshToken;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
