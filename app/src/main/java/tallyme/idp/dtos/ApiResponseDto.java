package tallyme.idp.dtos;

import java.io.Serializable;
import org.springframework.lang.Nullable;

public class ApiResponseDto implements Serializable {

  private static final long serialVersionUID = -614256360571915173L;

  @Nullable
  private String errorMessage;

  public ApiResponseDto() {}

  public ApiResponseDto(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Nullable
  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(@Nullable String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
