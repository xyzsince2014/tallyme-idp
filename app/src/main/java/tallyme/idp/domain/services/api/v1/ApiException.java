package tallyme.idp.domain.services.api.v1;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

  private HttpStatus statusCode;
  private String errorMessage;

  public ApiException(HttpStatus statusCode, String errorMessage) {
    super(errorMessage);
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public HttpStatus getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(HttpStatus statusCode) {
    this.statusCode = statusCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
