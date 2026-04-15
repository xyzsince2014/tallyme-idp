package tallyme.idp.aspects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers, HttpStatus status, WebRequest req) {
    ApiError apiError = new ApiError();
    apiError.setMessage(e.getMessage());
    apiError.setDocumentationUrl("https://localhost/auth/api/errors"); // todo: use an environmental variable

    return super.handleExceptionInternal(e, apiError, headers, status, req);
  }

  @ExceptionHandler
  public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException e, WebRequest req) {
    return handleExceptionInternal(e, null, null, HttpStatus.NOT_FOUND, req);
  }

}
