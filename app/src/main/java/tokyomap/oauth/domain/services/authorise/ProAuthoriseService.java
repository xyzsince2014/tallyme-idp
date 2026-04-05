package tokyomap.oauth.domain.services.authorise;

import java.net.URI;
import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tokyomap.oauth.application.authorise.AuthorisationForm;
import tokyomap.oauth.domain.entities.postgres.Usr;
import tokyomap.oauth.domain.entities.redis.PreAuthoriseCache;
import tokyomap.oauth.domain.entities.redis.ProAuthoriseCache;
import tokyomap.oauth.domain.logics.RedisLogic;

@Service
public class ProAuthoriseService {

  private final String responseTypeCode;
  private final RedisLogic redisLogic;

  @Autowired
  public ProAuthoriseService(
    RedisLogic redisLogic,
    @Value("${oauth.response.type.code}") String responseTypeCode
  ) {
    this.redisLogic = redisLogic;
    this.responseTypeCode = responseTypeCode;
  }

  /**
   * Executes authorisation for the given authorisationForm.
   *
   * @param
   * @param authorisationForm
   * @return redirectUri
   */
  @Transactional
  public URI execute(Usr resourceOwner, AuthorisationForm authorisationForm) throws InvalidProAuthoriseException {

    AuthenticationResult authenticationResult = this.authenticate(resourceOwner, authorisationForm);

    switch (authenticationResult.getAuthorisationRequest().getResponseType()) {
      case "code": {
        // Authorisation Code Flow
        URI redirectUri = this.issueCode(authenticationResult);
        return redirectUri;
      }
      default:
        throw new InvalidProAuthoriseException("Invalid Response Type");
    }
  }

  /**
   * Authenticates the user credential, and fetches the preAuthoriseCache for the requestId.
   *
   * @param authorisationForm
   * @return AuthenticationResult
   */
  private AuthenticationResult authenticate(Usr resourceOwner, AuthorisationForm authorisationForm) {

    String requestId = authorisationForm.getRequestId();

    PreAuthoriseCache preAuthoriseCache = this.redisLogic.getPreAuthoriseCache(requestId);

    if (preAuthoriseCache == null) {
      throw new InvalidProAuthoriseException("Invalid RequestId");
    }

    String requestedScope = preAuthoriseCache.getScope();

    if(!resourceOwner.getScopeList().containsAll(Arrays.asList(requestedScope.split(" ")))) {
      throw new InvalidProAuthoriseException("Invalid Scopes Requested");
    }

    return new AuthenticationResult(resourceOwner, requestedScope, preAuthoriseCache);
  }

  /**
   * Issues an Authorisation Code and caches the associated info.
   *
   * @param authenticationResult
   * @return redirectUri
   */
  private URI issueCode(AuthenticationResult authenticationResult) {
    String sub = authenticationResult.getResourceOwner().getSub();
    String requestedScope = authenticationResult.getScopesRequested();
    PreAuthoriseCache preAuthoriseCache = authenticationResult.getAuthorisationRequest();

    String code = RandomStringUtils.random(8, true, true);
    
    ProAuthoriseCache proAuthoriseCache = new ProAuthoriseCache(sub, requestedScope, preAuthoriseCache);
    this.redisLogic.saveProAuthoriseCache(code, proAuthoriseCache);

    URI redirectUri = UriComponentsBuilder
        .fromUriString(preAuthoriseCache.getRedirectUri())
        .queryParam("code", code)
        .queryParam("state", preAuthoriseCache.getState())
        .build()
        .toUri();

    return redirectUri;
  }

  private class AuthenticationResult {
    private Usr resourceOwner;
    private String requestedScope;
    private PreAuthoriseCache preAuthoriseCache;

    AuthenticationResult(Usr resourceOwner, String requestedScope, PreAuthoriseCache preAuthoriseCache) {
      this.resourceOwner = resourceOwner;
      this.requestedScope = requestedScope;
      this.preAuthoriseCache = preAuthoriseCache;
    }

    public Usr getResourceOwner() {
      return resourceOwner;
    }

    public void setResourceOwner(Usr resourceOwner) {
      this.resourceOwner = resourceOwner;
    }

    public String getScopesRequested() {
      return requestedScope;
    }

    public void setScopesRequested(String requestedScope) {
      this.requestedScope = requestedScope;
    }

    public PreAuthoriseCache getAuthorisationRequest() {
      return preAuthoriseCache;
    }

    public void setAuthorisationRequest(PreAuthoriseCache preAuthoriseCache) {
      this.preAuthoriseCache = preAuthoriseCache;
    }
  }
}
