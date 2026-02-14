package tokyomap.oauth.dtos;

import java.io.Serializable;

public class RevokeRequestDto implements Serializable {

  private static final long serialVersionUID = 4257687656585158577L;

  private String token;

  private String tokenTypeHint;


  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getTokenTypeHint() {
    return tokenTypeHint;
  }

  public void setTokenTypeHint(String tokenTypeHint) {
    this.tokenTypeHint = tokenTypeHint;
  }

  @Override
  public String toString() {
    return "token = " + this.token + ", tokenTypeHint = " + this.tokenTypeHint;
  }
}
