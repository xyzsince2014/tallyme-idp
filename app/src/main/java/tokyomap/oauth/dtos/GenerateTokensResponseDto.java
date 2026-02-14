package tokyomap.oauth.dtos;

import java.io.Serializable;

public class GenerateTokensResponseDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = 8788792708297075355L;

  private String tokenType;
  private String accessToken;
  private String refreshToken;
  private String idToken;
  private String scopes;

  public GenerateTokensResponseDto(String errorMessage) {
    super(errorMessage);
  }

  /**
   * Constructor for the Client Credentials Flow where no refresh token or ID token is issued.
   *
   * @param tokenType
   * @param accessToken
   * @param scopes
   */
  public GenerateTokensResponseDto(String tokenType, String accessToken, String scopes) {
    this.tokenType = tokenType;
    this.accessToken = accessToken;
    this.refreshToken = null;
    this.idToken = null;
    this.scopes = scopes;
  }

  /**
   * Constructor for the Auth Code Flow and the Refresh Token Flow.
   *
   * @param tokenType
   * @param accessToken
   * @param refreshToken
   * @param idToken
   * @param scopes
   */
  public GenerateTokensResponseDto(String tokenType, String accessToken, String refreshToken, String idToken, String scopes) {
    this.tokenType = tokenType;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.idToken = idToken;
    this.scopes = scopes;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getIdToken() {
    return idToken;
  }

  public void setIdToken(String idToken) {
    this.idToken = idToken;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scope) {
    this.scopes = scopes;
  }
}
