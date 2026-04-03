package tokyomap.oauth.dtos;

/**
 * DTO that carries the result of token endpoint validation through to token generation.
 *
 * @param <T> the type of the authorisation payload cached in Redis,
 *           e.g. {@link tokyomap.oauth.domain.entities.redis.ProAuthoriseCache} for the Authorisation Code Flow.
 */
public class TokenValidationResultDto<T> {

  /** The client ID of the validated client. */
  private String clientId;

  /** The authorisation payload fetched from Redis, carrying sub, scope, and the original authorisation request. */
  private T payload;

  /** The auth code used to redeem this token request, by which code can be removed from Redis after a successful token exchange. */
  private String code;

  /**
   * Constructor for flows that do not use an auth code (e.g. Implicit Flow, Refresh Token Flow).
   *
   * @param clientId the validated client ID
   * @param payload  the authorisation payload fetched from Redis
   */
  public TokenValidationResultDto(String clientId, T payload) {
    this.clientId = clientId;
    this.payload = payload;
  }

  /**
   * Constructor for the Authorisation Code Flow.
   * Carries the code so that it can be deleted from Redis after a successful token exchange, enforcing single-use per RFC 6749 §4.1.2.
   *
   * @param clientId the validated client ID
   * @param payload  the authorisation payload fetched from Redis
   * @param code     the auth code to be deleted after use
   */
  public TokenValidationResultDto(String clientId, T payload, String code) {
    this.clientId = clientId;
    this.payload = payload;
    this.code = code;
  }

  /**
   * @return the validated client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * @param clientId the validated client ID
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * @return the authorisation payload fetched from Redis
   */
  public T getPayload() { return payload; }

  /**
   * @param payload the authorisation payload fetched from Redis
   */
  public void setPayload(T payload) { this.payload = payload; }

  /**
   * @return the auth code used to redeem this token request, or null for flows that do not use an auth code
   */
  public String getCode() { return code; }

  /**
   * @param code the auth code used to redeem this token request
   */
  public void setCode(String code) { this.code = code; }
}
