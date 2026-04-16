package tallyme.idp.dtos;

import java.io.Serializable;

public class UpdateClientResponseDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = -2214581005531947697L;

  private ResponseClientDto client;

  public UpdateClientResponseDto(String errorMessage) {
    super(errorMessage);
  }

  public UpdateClientResponseDto(ResponseClientDto responseClientDto) {
    this.client = responseClientDto;
  }

  public ResponseClientDto getClient() {
    return client;
  }

  public void setClient(ResponseClientDto client) {
    this.client = client;
  }
}
