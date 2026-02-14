package tokyomap.oauth.domain.logics;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tokyomap.oauth.domain.entities.redis.PreAuthoriseCache;
import tokyomap.oauth.domain.entities.redis.ProAuthoriseCache;

@Component
public class RedisLogic {

  private static final int CODE_LIFETIME = 10;

  private final RedisTemplate<String, PreAuthoriseCache> preAuthoriseCacheRedisTemplate;
  private final RedisTemplate<String, ProAuthoriseCache> proAuthoriseCacheRedisTemplate;

  @Autowired
  public RedisLogic(
      RedisTemplate<String, PreAuthoriseCache> preAuthoriseCacheRedisTemplate,
      RedisTemplate<String, ProAuthoriseCache> proAuthoriseCacheRedisTemplate
  ) {
    this.preAuthoriseCacheRedisTemplate = preAuthoriseCacheRedisTemplate;
    this.proAuthoriseCacheRedisTemplate = proAuthoriseCacheRedisTemplate;
  }

  public PreAuthoriseCache getPreAuthoriseCache(String key) {
    return this.preAuthoriseCacheRedisTemplate.opsForValue().get(key);
  }

  public void savePreAuthoriseCache(String key, PreAuthoriseCache preAuthoriseCache) {
    this.preAuthoriseCacheRedisTemplate.opsForValue().set(key, preAuthoriseCache);
  }

  public ProAuthoriseCache getProAuthoriseCache(String key) {
    return this.proAuthoriseCacheRedisTemplate.opsForValue().get(key);
  }

  public void saveProAuthoriseCache(String key, ProAuthoriseCache proAuthoriseCache) {
    this.proAuthoriseCacheRedisTemplate.opsForValue().set(key, proAuthoriseCache);
    this.proAuthoriseCacheRedisTemplate.expire(key, CODE_LIFETIME, TimeUnit.MINUTES);
  }

  /**
   * Deletes the ProAuthoriseCache for the given code.
   *
   * @param code
   */
  public void deleteProAuthoriseCache(String code) {
    this.proAuthoriseCacheRedisTemplate.delete(code);
  }
}
