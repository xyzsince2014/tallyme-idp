package tokyomap.oauth.dtos;

import java.io.Serializable;
import java.lang.reflect.Array;
import javax.annotation.Nullable;

public class GenerateTokensRequestDto implements Serializable {

  private static final long serialVersionUID = 8971927453438048026L;

  private String grantType;

  @Nullable
  private String code;

  @Nullable
  private String redirectUri;

  @Nullable
  private String codeVerifier;

  @Nullable
  private String clientId;

  @Nullable
  private String clientSecret;

  @Nullable
  private String refreshToken;

  // todo: rename to `scope`
  @Nullable
  private String[] scopes;

  public String getGrantType() {
    return grantType;
  }

  public void setGrantType(String grantType) {
    this.grantType = grantType;
  }

  @Nullable
  public String getCode() {
    return code;
  }

  public void setCode(@Nullable String code) {
    this.code = code;
  }

  @Nullable
  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(@Nullable String redirectUri) {
    this.redirectUri = redirectUri;
  }

  @Nullable
  public String getCodeVerifier() {
    return codeVerifier;
  }

  public void setCodeVerifier(@Nullable String codeVerifier) {
    this.codeVerifier = codeVerifier;
  }

  @Nullable
  public String getClientId() {
    return clientId;
  }

  public void setClientId(@Nullable String clientId) {
    this.clientId = clientId;
  }

  @Nullable
  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(@Nullable String clientSecret) {
    this.clientSecret = clientSecret;
  }

  @Nullable
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Nullable
  public String[] getScopes() {
    return scopes;
  }

  public void setScopes(@Nullable String[] scopes) {
    this.scopes = scopes;
  }

  @Override
  public String toString() {
    return "grantType = " + this.grantType
      + ", code = " + this.code
      + ", redirectUri = " + this.redirectUri
      + ", clientId = " + this.clientId
      + ", String.join(\" \", this.scopes) = " + String.join(" ", this.scopes);
  }
}
