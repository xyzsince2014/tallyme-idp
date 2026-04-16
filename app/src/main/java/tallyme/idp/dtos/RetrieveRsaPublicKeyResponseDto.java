package tallyme.idp.dtos;

import java.io.Serializable;

public class RetrieveRsaPublicKeyResponseDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = 8371351274901386557L;

  private String pemPublicKey;

  public RetrieveRsaPublicKeyResponseDto() {}

  public RetrieveRsaPublicKeyResponseDto(String pemPublicKey) {
    this.pemPublicKey = pemPublicKey;
  }

  public RetrieveRsaPublicKeyResponseDto(String errorMessage, String pemPublicKey) {
    super(errorMessage);
    this.pemPublicKey = pemPublicKey;
  }

  public String getPemPublicKey() {
    return pemPublicKey;
  }

  public void setPemPublicKey(String pemPublicKey) {
    this.pemPublicKey = pemPublicKey;
  }
}
