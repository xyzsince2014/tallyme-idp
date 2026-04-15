package tallyme.idp.dtos;

import java.io.Serializable;

public class UpdateClientRequestDto implements Serializable {

  private static final long serialVersionUID = -5282319747583266650L;

  private RequestClientDto client;

  public RequestClientDto getClient() {
    return client;
  }

  public void setClient(RequestClientDto requestClientDto) {
    this.client = requestClientDto;
  }
}
