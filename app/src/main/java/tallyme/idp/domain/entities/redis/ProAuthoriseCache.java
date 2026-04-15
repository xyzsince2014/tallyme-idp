package tallyme.idp.domain.entities.redis;

import java.io.Serializable;

public class ProAuthoriseCache implements Serializable {

  private static final long serialVersionUID = -5514415646648723349L;

  private String sub;
  private String scopeRequested;
  private PreAuthoriseCache preAuthoriseCache;

  // used to deserialization by RedisTemplate
  public ProAuthoriseCache() {}

  public ProAuthoriseCache(String sub, String scopeRequested, PreAuthoriseCache preAuthoriseCache) {
    this.sub = sub;
    this.scopeRequested = scopeRequested;
    this.preAuthoriseCache = preAuthoriseCache;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public String getScopeRequested() {
    return scopeRequested;
  }

  public void setScopeRequested(String scopeRequested) {
    this.scopeRequested = scopeRequested;
  }

  public PreAuthoriseCache getPreAuthoriseCache() {
    return preAuthoriseCache;
  }

  public void setPreAuthoriseCache(PreAuthoriseCache preAuthoriseCache) {
    this.preAuthoriseCache = preAuthoriseCache;
  }

  @Override
  public String toString() {
    return "sub = " + this.sub
        + ", scopeRequested = " + this.scopeRequested
        + ", preAuthoriseCache = " + this.preAuthoriseCache.toString();
  }
}
