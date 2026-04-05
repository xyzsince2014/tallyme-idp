package tokyomap.oauth.dtos;

import java.io.Serializable;
import tokyomap.oauth.domain.entities.postgres.Client;

public class PreAuthoriseResponseDto implements Serializable {

  private static final long serialVersionUID = -3731207544869794764L;

  private Client client;
  private String requestId;
  private String requestedScope;

  public PreAuthoriseResponseDto(Client client, String requestId, String requestedScope) {
    this.client = client;
    this.requestId = requestId;
    this.requestedScope = requestedScope;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getRequestedScope() {
    return requestedScope;
  }

  public void setRequestedScope(String requestedScope) {
    this.requestedScope = requestedScope;
  }
}
