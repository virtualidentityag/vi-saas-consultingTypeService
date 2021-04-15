package de.caritas.cob.consultingtypeservice.config;

import de.caritas.cob.consultingtypeservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.consultingtypeservice.filter.StatelessCsrfFilter;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * Provides the Keycloak/Spring Security configuration.
 */
@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  public static final String[] WHITE_LIST =
      new String[]{"/consultingtypes/docs", "/consultingtypes/docs/**", "/v2/api-docs", "/configuration/ui",
          "/swagger-resources/**", "/configuration/security", "/swagger-ui.html", "/webjars/**"};

  @SuppressWarnings("unused")
  private final KeycloakClientRequestFactory keycloakClientRequestFactory;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Autowired
  private Environment environment;

  /**
   * Processes HTTP requests and checks for a valid spring security authentication for the
   * (Keycloak) principal (authorization header).
   */
  public SecurityConfig(KeycloakClientRequestFactory keycloakClientRequestFactory) {
    this.keycloakClientRequestFactory = keycloakClientRequestFactory;
  }

  /**
   * Configure spring security filter chain: disable default Spring Boot CSRF token behavior and add
   * custom {@link StatelessCsrfFilter}, set all sessions to be fully stateless, define necessary
   * Keycloak roles for specific REST API paths
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.csrf().disable()
        .addFilterBefore(new StatelessCsrfFilter(csrfCookieProperty, csrfHeaderProperty),
            CsrfFilter.class)
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).and().authorizeRequests()
        .antMatchers("/consultingtypes/*").permitAll()
        .antMatchers(WHITE_LIST).permitAll()
        .antMatchers("/consultingtypes").permitAll()
        .anyRequest().denyAll();
  }

  /**
   * Use the KeycloakSpringBootConfigResolver to be able to save the Keycloak settings in the spring
   * application properties.
   */
  @Bean
  public KeycloakConfigResolver keyCloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  /**
   * Change springs authentication strategy to be stateless (no session is being created).
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  /**
   * Change the default AuthenticationProvider to KeycloakAuthenticationProvider and register it in
   * the spring security context. This maps the Kecloak roles to match the spring security roles
   * (prefix ROLE_).
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth,
      RoleAuthorizationAuthorityMapper authorityMapper) {
    KeycloakAuthenticationProvider keyCloakAuthProvider = keycloakAuthenticationProvider();
    keyCloakAuthProvider.setGrantedAuthoritiesMapper(authorityMapper);

    auth.authenticationProvider(keyCloakAuthProvider);
  }

  /**
   * From the Keycloag documentation: "Spring Boot attempts to eagerly register filter beans with
   * the web application context. Therefore, when running the Keycloak Spring Security adapter in a
   * Spring Boot environment, it may be necessary to add FilterRegistrationBeans to your security
   * configuration to prevent the Keycloak filters from being registered twice."
   *
   * https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/oidc/java/spring-security-adapter.adoc
   *
   * {@link package.class#member label}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
      KeycloakAuthenticationProcessingFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
      KeycloakPreAuthActionsFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticatedActionsFilterBean(
      KeycloakAuthenticatedActionsFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakSecurityContextRequestFilterBean(
      KeycloakSecurityContextRequestFilter filter) {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }
}
