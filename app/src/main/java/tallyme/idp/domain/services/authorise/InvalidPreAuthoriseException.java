package tallyme.idp.domain.services.authorise;

public class InvalidPreAuthoriseException extends RuntimeException {

  private String clientUri;

  public InvalidPreAuthoriseException(String message, String clientUri) {
    super("InvalidPreAuthoriseException: " + message);
    this.clientUri = clientUri;
  }

  public String getClientUri() {
    return this.clientUri;
  }
}
