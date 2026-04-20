package tallyme.idp.dtos;

import java.io.Serializable;

public class ReadClientResponseDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = 966095348686127071L;

  private ResponseClientDto client;

  public ReadClientResponseDto(String errorMessage) {
    super(errorMessage);
  }

  public ReadClientResponseDto(ResponseClientDto responseClientDto) {
    this.client = responseClientDto;
  }

  public ResponseClientDto getClient() {
    return client;
  }

  public void setClient(ResponseClientDto client) {
    this.client = client;
  }
}
