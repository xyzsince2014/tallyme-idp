package tallyme.idp.dtos;

import java.io.Serializable;

public class ClientValidationResultDto implements Serializable {

  private static final long serialVersionUID = 8054620885431918882L;

  private String[] grantTypes;
  private String[] responseTypes;
  private String tokenEndpointAuthMethod;

  public ClientValidationResultDto() {}

  public ClientValidationResultDto(String[] grantTypes, String[] responseTypes, String tokenEndpointAuthMethod) {
    this.grantTypes = grantTypes;
    this.responseTypes = responseTypes;
    this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
  }

  public String[] getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(String[] grantTypes) {
    this.grantTypes = grantTypes;
  }

  public String[] getResponseTypes() {
    return responseTypes;
  }

  public void setResponseTypes(String[] responseTypes) {
    this.responseTypes = responseTypes;
  }

  public String getTokenEndpointAuthMethod() {
    return tokenEndpointAuthMethod;
  }

  public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
    this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
  }
}
