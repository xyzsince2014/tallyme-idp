package tallyme.idp.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.Nullable;

public class ResponseClientDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = -5825872791496453030L;

  @Nullable
  private String clientId;

  @Nullable
  private String clientSecret;

  /** a human-readable display name for the client. */
  private String clientName;

  /** the URI that indicates the client’s homepage. */
  private String clientUri;

  private String[] redirectUris;

  /** a URI for a graphical logo for the client. The authorisation server can use this URL to display a logo for the client to the user. */
  @Nullable
  private String logoUri;

  /** a list of ways to contact the people responsible for a client, usually email addresses, but they could be phone numbers, instant messaging addresses, or other contact mechanisms. */
  @Nullable
  private String[] contacts;

  /** a URI for a human-readable page which lists the terms of service for the client. */
  @Nullable
  private String tosUri;

  /** a URI for a human-readable page that contains the privacy policy for the client. */
  @Nullable
  private String policyUri;

  /** a URI which points to the JSON Web Key Set containing the public keys for this client, hosted in a place accessible to the authorisation server. This field can’t be used along with the `jwks` field. The jwksUri field is preferred, as it allows the client to rotate keys. */
  @Nullable
  private String jwksUri;

  /** a unique identifier for the software that the client is running. This identifier will be the same across all instances of a given piece of client software. */
  @Nullable
  private String softwareId;

  /** a version identifier for the client software indicated by the software_id field. */
  @Nullable
  private String softwareVersion;

  private String[] grantTypes;

  private String[] responseTypes;

  private String tokenEndpointAuthMethod;

  private String[] scope;

  @Nullable
  private String registrationAccessToken;

  @Nullable
  private String registrationClientUri;

  @Nullable
  private LocalDateTime createdAt;

  @Nullable
  private LocalDateTime expiresAt;

  public ResponseClientDto() {}

  public ResponseClientDto(String errorMessage) {
    super(errorMessage);
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

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getClientUri() {
    return clientUri;
  }

  public void setClientUri(String clientUri) {
    this.clientUri = clientUri;
  }

  public String[] getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(String[] redirectUris) {
    this.redirectUris = redirectUris;
  }

  @Nullable
  public String getLogoUri() {
    return logoUri;
  }

  public void setLogoUri(@Nullable String logoUri) {
    this.logoUri = logoUri;
  }

  @Nullable
  public String[] getContacts() {
    return contacts;
  }

  public void setContacts(@Nullable String[] contacts) {
    this.contacts = contacts;
  }

  @Nullable
  public String getTosUri() {
    return tosUri;
  }

  public void setTosUri(@Nullable String tosUri) {
    this.tosUri = tosUri;
  }

  @Nullable
  public String getPolicyUri() {
    return policyUri;
  }

  public void setPolicyUri(@Nullable String policyUri) {
    this.policyUri = policyUri;
  }

  @Nullable
  public String getJwksUri() {
    return jwksUri;
  }

  public void setJwksUri(@Nullable String jwksUri) {
    this.jwksUri = jwksUri;
  }

  @Nullable
  public String getSoftwareId() {
    return softwareId;
  }

  public void setSoftwareId(@Nullable String softwareId) {
    this.softwareId = softwareId;
  }

  @Nullable
  public String getSoftwareVersion() {
    return softwareVersion;
  }

  public void setSoftwareVersion(@Nullable String softwareVersion) {
    this.softwareVersion = softwareVersion;
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

  public String[] getScope() {
    return scope;
  }

  public void setScope(String[] scope) {
    this.scope = scope;
  }

  @Nullable
  public String getRegistrationAccessToken() {
    return registrationAccessToken;
  }

  public void setRegistrationAccessToken(@Nullable String registrationAccessToken) {
    this.registrationAccessToken = registrationAccessToken;
  }

  @Nullable
  public String getRegistrationClientUri() {
    return registrationClientUri;
  }

  public void setRegistrationClientUri(@Nullable String registrationClientUri) {
    this.registrationClientUri = registrationClientUri;
  }

  @Nullable
  public LocalDateTime getCreatedAt() { return createdAt; }

  public void setCreatedAt(@Nullable LocalDateTime createdAt) { this.createdAt = createdAt; }

  @Nullable
  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(@Nullable LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public String toString() {
    return "clientId = " + this.clientId
      + ", clientSecret = " + this.clientSecret
      + ", clientName = " + this.clientName
      + ", clientUri = " + this.clientUri
      + ", redirectUris = " + this.redirectUris.toString()
      + ", grantTypes = " + this.grantTypes
      + ", responseTypes = " + this.responseTypes
      + ", tokenEndpointAuthMethod = " + this.tokenEndpointAuthMethod
      + ", scope = " + this.scope
      + ", registrationAccessToken = " + this.registrationAccessToken
      + ", registrationClientUri = " + this.registrationClientUri
      + ", createdAt = " + this.createdAt
      + ", expiresAt = " + this.expiresAt.toString();
  }
}
