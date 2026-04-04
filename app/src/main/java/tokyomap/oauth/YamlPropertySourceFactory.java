package tokyomap.oauth;

import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Custom {@link PropertySourceFactory} that enables {@code @PropertySource} to load YAML files.
 * Spring MVC does not support YAML natively — this factory bridges that gap by using
 * {@link YamlPropertiesFactoryBean} to parse the YAML and flatten it into a {@link Properties}
 * object, which Spring can then resolve via {@code @Value} as normal dot-separated keys.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

  /**
   * Parses the given YAML resource and returns it as a {@link PropertiesPropertySource}.
   * The YAML hierarchy is flattened to dot-separated keys (e.g. {@code oauth.token.type.bearer}).
   *
   * @param name the name to assign to the property source; falls back to the filename if blank
   * @param resource the encoded YAML resource to load
   * @return a {@link PropertySource} backed by the flattened YAML properties
   */
  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(resource.getResource());
    Properties properties = factory.getObject();
    String sourceName = (name != null && !name.isEmpty()) ? name : resource.getResource().getFilename();
    return new PropertiesPropertySource(sourceName, properties);
  }
}
