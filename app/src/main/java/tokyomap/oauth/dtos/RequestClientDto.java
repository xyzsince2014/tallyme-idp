package tokyomap.oauth.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import javax.annotation.Nullable;

public class RequestClientDto implements Serializable {

  private static final long serialVersionUID = -5825872791496453030L;

  @Nullable
  private String clientId;

  @Nullable
  private String clientSecret;

  /**
   * How the client will authenticate to the token endpoint:
   * - none: the client does not authenticate to the token endpoint
   * - client_secret_base: the client sends its client secret with HTTP Basic (the default value)
   * - client_secret_post_: the client sends its client secret with HTTP form params
   * - client_secret_jwt: the client will create a JWT symmetrically signed with its client secret
   * - private_key_jwt: the client will create a JWT asymmetrically signed with its private key (the public key must be registered with the auth server)
   */
  private String tokenEndpointAuthMethod;

  /**
   * Which grant types the client will use to get tokens.
   * The values here are the same ones used at the token endpoint in the `grant_type` param.
   * - authorization_code: The authorization code grant, which needs to be used with "code" `response_type`
   * - the implicit grant, which needs "token" `response_type`
   * - password: the resource owner password grant, where the client prompts the resource owner for their username and password
   * - client_credentials: the client credentials grant, where the client uses its own credentials to obtain a token for itself
   * - refresh_token: the refresh token grant, where the client uses a refresh token to obtain a new access token when the resource owner is no longer present
   */
  private String[] grantTypes;

  /**
   * An array of URI strings used in redirect-based OAuth grants, such authorization_code and implicit.
   */
  private String[] redirectUris;

  /**
   * Which response types the client will use at the authorization endpoint.
   * These values are the same as those used in the `response_type` param.
   * - code: the authorization endpoint returns an authorization code which needs to be handed in at the token endpoint to get a token
   * - token: the implicit response type, which returns a token directly to the redirect URI
   */
  private String[] responseTypes;

  /**
   * A list of scope which the client can use when requesting tokens.
   * Todo: this is a string of spaced-separated values, as in the OAuth protocol.
   */
  private String[] scope;

  /**
   * A human-readable display name for the client.
   */
  private String clientName;

  /**
   * The URI that indicates the client’s homepage.
   */
  private String clientUri;



  /** A URI for a graphical logo for the client. The authorisation server can use this URL to display a logo for the client to the user. */
  @Nullable
  private String logoUri;

  /**
   * A list of ways to contact the people responsible for a client.
   * Usually, they are email addresses, but could be phone numbers, instant messaging addresses, or other contact mechanisms.
   */
  @Nullable
  private String[] contacts;

  /**
   * A URI for a human-readable page which lists the terms of service for the client.
   * These terms describe the contractual relationship that the resource owner accepts when authorizing the client.
   */
  @Nullable
  private String tosUri;

  /**
   * A URI for a human-readable page that contains the privacy policy for the client.
   */
  @Nullable
  private String policyUri;

  /**
   * A URI which points to the JSON Web Key Set containing the public keys for this client, hosted in a place accessible to the authorisation server.
   * This field can’t be used along with the `jwks` field.
   * The `jwksUri` field is preferred, as it allows the client to rotate keys.
   */
  @Nullable
  private String jwksUri;

  /**
   * A JSON Web Key Set document (a JSON object) containing the public keys for this client.
   * This field can't be used along with the `jwks_uri` field.
   * The `jwksUri` field is preferred, as it allows the client to rotate keys.
   */
  @Nullable
  private String jwks;

  /**
   * A unique identifier for the software that the client is running.
   * This identifier will be the same across all instances of a given piece of client software.
   */
  @Nullable
  private String softwareId;

  /**
   * A version identifier for the client software indicated by the software_id field.
   * The version string is opaque to the authorization sever, and no particular format is assumed.
   */
  @Nullable
  private String softwareVersion;

  /**
   * An OAuth bearer token that the client can use to access the client configuration endpoint.
   */
  @Nullable
  private String registrationAccessToken;

  /**
   * A client configuration endpoint URI, which provides all the management functionality for this specific client.
   */
  @Nullable
  private String registrationClientUri;

  @Nullable
  private LocalDateTime expiresAt;

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

  public String getTokenEndpointAuthMethod() {
    return tokenEndpointAuthMethod;
  }

  public void setTokenEndpointAuthMethod(String tokenEndpointAuthMethod) {
    this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
  }

  public String[] getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(String[] grantTypes) {
    this.grantTypes = grantTypes;
  }

  public String[] getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(String[] redirectUris) {
    this.redirectUris = redirectUris;
  }

  public String[] getResponseTypes() {
    return responseTypes;
  }

  public void setResponseTypes(String[] responseTypes) {
    this.responseTypes = responseTypes;
  }

  public String[] getScope() {
    return scope;
  }

  public void setScope(String[] scope) {
    this.scope = scope;
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
  public String getJwks() {
    return jwks;
  }

  public void setJwks(@Nullable String jwks) {
    this.jwks = jwks;
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
      + ", redirectUris = " + Arrays.toString(this.redirectUris)
      + ", grantTypes = " + Arrays.toString(this.grantTypes)
      + ", responseTypes = " + Arrays.toString(this.responseTypes)
      + ", tokenEndpointAuthMethod = " + this.tokenEndpointAuthMethod
      + ", scope = " + Arrays.toString(this.scope)
      + ", registrationAccessToken = " + this.registrationAccessToken
      + ", registrationClientUri = " + this.registrationClientUri
      + ", expiresAt = " + this.expiresAt.toString();
  }
}
