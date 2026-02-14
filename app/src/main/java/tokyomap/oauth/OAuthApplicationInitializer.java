package tokyomap.oauth;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Implementing WebApplicationInitializer is the minimal configuration to work Spring Framework,
 * extending AbstractHttpSessionApplicationInitializer enables Spring Session with Redis
 *
 * Exactly speaking, by extending AbstractHttpSessionApplicationInitializer,
 * we ensure that the Spring Bean by the name springSessionRepositoryFilter is registered with our Servlet Container
 * for every request before Spring Security’s springSecurityFilterChain.
 */
public class OAuthApplicationInitializer extends AbstractHttpSessionApplicationInitializer {

  /**
   * @see @link{https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/WebApplicationInitializer.html}
   * @see @link{https://www.baeldung.com/spring-web-contexts}
   * @param servletContext
   * @throws ServletException
   */
  @Override
  public void onStartup(ServletContext servletContext) {

    super.onStartup(servletContext);

    // todo: webApplicationContext should have business logics only, e.g. services, repositories, ORMs and data sources
    AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
    webApplicationContext.setConfigLocations(PostgresJpaConfig.class.getName());
    webApplicationContext.setConfigLocation(WebSecurityConfig.class.getName());
    webApplicationContext.setConfigLocation(ThymeleafConfig.class.getName());
    webApplicationContext.setConfigLocation(WebMvcConfig.class.getName());
    webApplicationContext.registerShutdownHook();
    servletContext.addListener(new ContextLoaderListener(webApplicationContext));

    // todo: dispatcherServletContext should have have controllers and views
    AnnotationConfigWebApplicationContext dispatcherServletContext =  new AnnotationConfigWebApplicationContext();
    // todo: dispatcherServletContext.setConfigLocation(DispatcherServletConfig.class.getName());
    Dynamic dispatcherServlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(dispatcherServletContext));
    dispatcherServlet.setLoadOnStartup(1);
    // todo: dispatcherServlet.setAsyncSupported(true);
    dispatcherServlet.addMapping("/");

    servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy()).addMappingForUrlPatterns(null, true,"/*");
    servletContext.addFilter("hiddenHttpMethodFilter", new HiddenHttpMethodFilter()).addMappingForUrlPatterns(null, true, "/*");
  }
}
