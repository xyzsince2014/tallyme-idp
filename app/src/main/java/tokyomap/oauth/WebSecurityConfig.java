package tokyomap.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import tokyomap.oauth.domain.services.authenticate.AuthenticateService;

@Configuration
@ComponentScan
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter { // todo: use SpringSecurity@5.7

  private final AuthenticateService authenticateService;

  @Autowired
  public WebSecurityConfig (AuthenticateService authenticateService) {
    this.authenticateService = authenticateService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    // Referrer-Policy: no-referrer, which prevents the `code` issued by the AS never from being leaked in the Referrer header.
    http.headers()
      .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER);

    http.authorizeRequests()
        .antMatchers("/css/**", "/img/**", "/js/**").permitAll()
        .antMatchers("/api/**").permitAll()
        .antMatchers("/authenticate/**", "/sign-up").not().authenticated()
        .anyRequest().authenticated();

    http.formLogin()
        .loginPage("/authenticate")
        .loginProcessingUrl("/authenticate")
        .usernameParameter("email")
        .passwordParameter("password")
        .failureUrl("/authenticate?error=true");

    http.logout()
        .logoutUrl("/sign-out/pro")
        .deleteCookies("JSESSIONID");

    http.sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
      .sessionFixation().newSession(); // renew session id whenever the user signs in

    // whitelist APIs which are requested with tokens or client credentials
    http.csrf().ignoringAntMatchers("/api/v1/**");
  }

  /**
   * Enables DaoAuthenticationProvider with the passwordEncoder.
   *
   * @param builder
   * @throws Exception
   */
  @Autowired
  protected void configureAuthenticationManager(AuthenticationManagerBuilder builder) throws Exception {
    builder.userDetailsService(this.authenticateService).passwordEncoder(passwordEncoder());
  }
}
