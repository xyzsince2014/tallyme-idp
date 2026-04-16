package tallyme.idp.dtos;

import java.io.Serializable;

public class IntrospectResponseDto extends ApiResponseDto implements Serializable {

  private static final long serialVersionUID = -2461708330251533171L;

  /** Whether the token is valid. */
  private boolean active;

  /** Subject. */
  private String sub;

  /**
   * Granted scope.
   * Per RFC 7662, scope in the introspection response is a single space-separated String, not an array.
   */
  private String scope;

  /**
   * Intended audience.
   * RFC 7662 inherits from RFC 7519 which says `aud` can be either a single string or an array.
   */
  private String aud;

  public IntrospectResponseDto(boolean active) {
    this.active = active;
  }

  public IntrospectResponseDto(String errorMessage, boolean active) {
    super(errorMessage);
    this.active = active;
  }

  public IntrospectResponseDto(boolean active, String sub, String scope, String aud) {
    this.active = active;
    this.sub = sub;
    this.scope = scope;
    this.aud = aud;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getAud() {
    return aud;
  }

  public void setAud(String aud) {
    this.aud = aud;
  }
}
