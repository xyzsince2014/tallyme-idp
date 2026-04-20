package tallyme.idp.dtos;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CredentialsDto implements Serializable {

  private static final long serialVersionUID = 6501945367821634948L;

  private String id;

  private String secret;

  private String scope;

  public CredentialsDto() {}

  public CredentialsDto(String id, String secret) {
    this.id = id;
    this.secret = secret;
  }

  public CredentialsDto(String id, String secret, String scope) {
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

  public String getScope() {
    return scope;
  }

  public List<String> getScopeList() {
    if (this.scope == null || this.scope.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.asList(this.scope.split(" "));
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}
