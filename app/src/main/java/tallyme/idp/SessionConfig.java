package tallyme.idp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60 * 60) // replaces HttpSession with SpringSession
public class SessionConfig {

  @Value("${REDIS_HOST}") private String host;
  @Value("${REDIS_PORT}") private String port;

  @Bean
  public static ConfigureRedisAction configureRedisAction() {
    return ConfigureRedisAction.NO_OP;
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(this.host, Integer.parseInt(this.port));
    return new JedisConnectionFactory(config);
  }

  /**
   * Manages session id in cookie.
   *
   * @return
   */
  @Bean
  public HttpSessionIdResolver httpSessionIdResolver() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setUseSecureCookie(true);
    serializer.setUseHttpOnlyCookie(true);
    serializer.setCookieMaxAge(60 * 60);
    serializer.setCookieName("JSESSIONID"); // todo: make a constant

    // CookieHttpSessionStrategy ではなく CookieHttpSessionIdResolver を使う
    CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
    resolver.setCookieSerializer(serializer);
    return resolver;
  }
}
