package tokyomap.oauth.domain.services.authorise;

import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokyomap.oauth.domain.entities.postgres.Client;
import tokyomap.oauth.domain.entities.redis.PreAuthoriseCache;
import tokyomap.oauth.domain.logics.ClientLogic;
import tokyomap.oauth.domain.logics.RedisLogic;
import tokyomap.oauth.dtos.PreAuthoriseResponseDto;

@Service
public class PreAuthoriseService {

  private final RedisLogic redisLogic;
  private final ClientLogic clientLogic;

  @Autowired
  public PreAuthoriseService(RedisLogic redisLogic, ClientLogic clientLogic) {
    this.redisLogic = redisLogic;
    this.clientLogic = clientLogic;
  }

  /**
   * Validates the given preAuthoriseCache, and cache it if valid.
   *
   * @param preAuthoriseCache
   * @return PreAuthoriseResponseDto
   * @exception throws Exception if request is invalid or something wrong takes place
   */
  @Transactional
  public PreAuthoriseResponseDto execute(PreAuthoriseCache preAuthoriseCache) throws InvalidPreAuthoriseException {
    ValidationResult validationResult = this.validateAuthorisationRequest(preAuthoriseCache);
    String requestId = this.cacheAuthorisationRequest(preAuthoriseCache);
    return new PreAuthoriseResponseDto(validationResult.getClient(), requestId, validationResult.getRequestedScope());
  }

  /**
   * Validates the preAuthoriseCache.
   *
   * @param preAuthoriseCache
   * @return ValidationResult
   */
  private ValidationResult validateAuthorisationRequest(PreAuthoriseCache preAuthoriseCache) {

    String clientId = preAuthoriseCache.getClientId();
    Client client = this.clientLogic.getClientByClientId(clientId);

    // RFC 6749 §4.1.2.1: if the client_id is invalid, the server MUST NOT redirect automatically
    // — there is no trustworthy redirect_uri, so inform the resource owner directly
    if (client == null) {
      throw new InvalidPreAuthoriseException("No Matching Client", null);
    }

    // todo: response_type is not validated here
    // RFC 6749 §4.1.1 requires response_type=code, and an invalid value should be rejected at this stage before the request is cached.

    // RFC 6749 §4.1.2: redirect_uri must exactly match one of the registered redirect_uris
    // — substring match is insufficient as it is vulnerable to open redirect (e.g. attacker embeds the registered uri as a query param)
    if (!Arrays.asList(client.getRedirectUris().split(" ")).contains(preAuthoriseCache.getRedirectUri())) {
      throw new InvalidPreAuthoriseException("Invalid Redirect URI", client.getClientUri());
    }

    String requestedScope = preAuthoriseCache.getScope();

    // RFC 6749 §3.3: the requested scope must be a subset of the scopes the client is registered for
    if(!client.getScopeList().containsAll(Arrays.asList(requestedScope.split(" ")))) {
      throw new InvalidPreAuthoriseException("Invalid Scopes Requested", client.getClientUri());
    }

    // todo: code_challenge and code_challenge_method are cached but never validated
    // RFC 7636 §4.3 requires the server to reject unsupported or missing code_challenge_method,
    // and public clients should be required to supply code_challenge at all

    // todo: state is not checked for presence
    // RFC 6749 §10.12 recommends state as the primary CSRF defence and it should be enforced

    // OpenID Connect Core §3.1.2.1: nonce is REQUIRED for the Hybrid Flow and RECOMMENDED for the
    // Authorisation Code Flow to bind the ID Token to the authorisation request and prevent replay attacks
    if(preAuthoriseCache.getNonce() == null) {
      throw new InvalidPreAuthoriseException("No Nonce Given", client.getClientUri());
    }

    return new ValidationResult(client, requestedScope);
  }

  /**
   * issue a requestId, and cache the preAuthoriseCache to Redis with it
   * @param preAuthoriseCache
   * @return requestId
   */
  private String cacheAuthorisationRequest(PreAuthoriseCache preAuthoriseCache) {
    String requestId = RandomStringUtils.random(8, true, true);
    this.redisLogic.savePreAuthoriseCache(requestId, preAuthoriseCache);
    return requestId;
  }

  /**
   * validation result
   */
  private class ValidationResult {
    private Client client;
    private String requestedScope;

    ValidationResult(Client client, String requestedScope) {
      this.client = client;
      this.requestedScope = requestedScope;
    }

    Client getClient() {
      return client;
    }

    String getRequestedScope() {
      return requestedScope;
    }
  }
}
