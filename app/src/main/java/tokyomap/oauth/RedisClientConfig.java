package tokyomap.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tokyomap.oauth.domain.entities.redis.PreAuthoriseCache;
import tokyomap.oauth.domain.entities.redis.ProAuthoriseCache;

@Configuration
@PropertySource("classpath:conf/redis.properties")
public class RedisClientConfig {

  @Value("${redis.jedis.host}") private String host;
  @Value("${redis.jedis.port}") private String port;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
    jedisConnectionFactory.setHostName(this.host);
    jedisConnectionFactory.setPort(Integer.parseInt(this.port));
    return jedisConnectionFactory;
  }

  @Bean
  public StringRedisTemplate jedisRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
    StringRedisTemplate redisTemplate = new StringRedisTemplate();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    return redisTemplate;
  }

  // todo: use cacheManagers, cf. https://jappy.hatenablog.com/entry/2017/08/11/100701
  /**
   * the RedisTemplate<String, PreAuthoriseCache> bean with Jackson2JsonRedisSerializer
   * @param jedisConnectionFactory
   * @return redisTemplate
   */
  @Bean
  public RedisTemplate<String, PreAuthoriseCache> preAuthoriseCacheRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
    RedisTemplate<String, PreAuthoriseCache> redisTemplate = new RedisTemplate();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(PreAuthoriseCache.class));
    return redisTemplate;
  }

  /**
   * the RedisTemplate<String, ProAuthoriseCache> bean with Jackson2JsonRedisSerializer
   * @param jedisConnectionFactory
   * @return redisTemplate
   */
  @Bean
  public RedisTemplate<String, ProAuthoriseCache> proAuthoriseCacheRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
    RedisTemplate<String, ProAuthoriseCache> redisTemplate = new RedisTemplate();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(ProAuthoriseCache.class));
    return redisTemplate;
  }
}
