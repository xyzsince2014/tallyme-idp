package tallyme.idp.application.authorise;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class AuthorisationForm implements Serializable {

  private static final long serialVersionUID = 2928719149463279194L;

  @NotNull
  private String requestId;

  private String clientUri;

  public AuthorisationForm() {}

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getClientUri() { return clientUri; }

  public void setClientUri(String clientUri) { this.clientUri = clientUri; }
}
