package tokyomap.oauth.dtos;

import java.io.Serializable;

public class CredentialsDto implements Serializable {

  private static final long serialVersionUID = 6501945367821634948L;

  private String id;

  private String secret;

  private String[] scope;

  public CredentialsDto() {}

  public CredentialsDto(String id, String secret) {
    this.id = id;
    this.secret = secret;
  }

  public CredentialsDto(String id, String secret, String[] scope) {
    this.id = id;
    this.secret = secret;
    this.scope = scope;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public String[] getScope() {
    return scope;
  }

  public void setScope(String[] scope) {
    this.scope = scope;
  }
}
