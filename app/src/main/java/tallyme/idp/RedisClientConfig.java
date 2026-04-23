package tallyme.idp;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tallyme.idp.domain.entities.redis.PreAuthoriseCache;
import tallyme.idp.domain.entities.redis.ProAuthoriseCache;

@Configuration
@PropertySource("classpath:conf/redis.properties")
public class RedisClientConfig {

  @Value("${REDIS_HOST}") private String host;
  @Value("${REDIS_PORT}") private int port;
  @Value("${redis.jedis.connectionTimeout}") private int connectionTimeout;
  @Value("${redis.jedis.soTimeout}") private int soTimeout;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(this.host, this.port);

    JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
        .connectTimeout(Duration.ofMillis(this.connectionTimeout))
        .readTimeout(Duration.ofMillis(this.soTimeout))
        .build();

    return new JedisConnectionFactory(redisConfig, clientConfig);
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
