package tokyomap.oauth.dtos;

import java.io.Serializable;

public class GenerateTokensRequestDto implements Serializable {

  private static final long serialVersionUID = 8971927453438048026L;

  private String grantType;

  private String code;

  private String redirectUri;

  private String codeVerifier;

  private String clientId;

  private String clientSecret;

  private String refreshToken;

  private String scope;

  public String getGrantType() {
    return grantType;
  }

  public void setGrantType(String grantType) {
    this.grantType = grantType;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getCodeVerifier() {
    return codeVerifier;
  }

  public void setCodeVerifier(String codeVerifier) {
    this.codeVerifier = codeVerifier;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    return "grantType = " + this.grantType
      + ", code = " + this.code
      + ", redirectUri = " + this.redirectUri
      + ", clientId = " + this.clientId
      + ", scope = " + this.scope;
  }
}
