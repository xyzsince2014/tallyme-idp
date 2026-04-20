package tallyme.idp.dtos;

import java.io.Serializable;

public class RequestIntrospectDto implements Serializable {

  private static final long serialVersionUID = 8131840449051816831L;

  private String token;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
