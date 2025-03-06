// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import static java.util.Arrays.asList;
import static org.italiangrid.storm.webdav.authz.managers.UnanimousDelegatedManager.forVoters;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.italiangrid.storm.webdav.authn.ErrorPageAuthenticationEntryPoint;
import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.managers.ConsensusBasedManager;
import org.italiangrid.storm.webdav.authz.managers.FineGrainedAuthzManager;
import org.italiangrid.storm.webdav.authz.managers.FineGrainedCopyMoveAuthzManager;
import org.italiangrid.storm.webdav.authz.managers.LocalAuthzManager;
import org.italiangrid.storm.webdav.authz.managers.MacaroonAuthzManager;
import org.italiangrid.storm.webdav.authz.managers.UnanimousDelegatedManager;
import org.italiangrid.storm.webdav.authz.managers.WlcgScopeAuthzCopyMoveManager;
import org.italiangrid.storm.webdav.authz.managers.WlcgScopeAuthzManager;
import org.italiangrid.storm.webdav.authz.pdp.LocalAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.util.SaveAuthnAccessDeniedHandler;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.oauth.StormJwtAuthenticationConverter;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.WebDAVMethod;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.HstsConfig;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity(proxyTargetClass = true)
public class SecurityConfig {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

  private static final List<String> ALLOWED_METHODS;

  static {
    ALLOWED_METHODS = new ArrayList<>();
    ALLOWED_METHODS.add(HttpMethod.HEAD.name());
    ALLOWED_METHODS.add(HttpMethod.GET.name());
    ALLOWED_METHODS.add(HttpMethod.POST.name());
    ALLOWED_METHODS.add(HttpMethod.PUT.name());
    ALLOWED_METHODS.add(HttpMethod.DELETE.name());
    ALLOWED_METHODS.add(HttpMethod.OPTIONS.name());
    ALLOWED_METHODS.add(HttpMethod.PATCH.name());
    ALLOWED_METHODS.add(WebDAVMethod.PROPFIND.name());
    ALLOWED_METHODS.add(WebDAVMethod.PROPPATCH.name());
    ALLOWED_METHODS.add(WebDAVMethod.MKCOL.name());
    ALLOWED_METHODS.add(WebDAVMethod.COPY.name());
    ALLOWED_METHODS.add(WebDAVMethod.MOVE.name());
    ALLOWED_METHODS.add(WebDAVMethod.LOCK.name());
    ALLOWED_METHODS.add(WebDAVMethod.UNLOCK.name());
  }

  @Autowired OAuthProperties oauthProperties;

  @Autowired StorageAreaConfiguration saConfiguration;

  @Autowired ServiceConfigurationProperties serviceConfigurationProperties;

  @Autowired PathResolver pathResolver;

  @Autowired
  @Qualifier("vomsAuthenticationFilter")
  VOMSAuthenticationFilter vomsFilter;

  @Autowired LocalURLService localURLService;

  @Autowired PathAuthorizationPdp fineGrainedAuthzPdp;

  @Autowired PrincipalHelper principalHelper;

  @Bean
  HttpFirewall allowWebDAVMethodsFirewall() {

    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(ALLOWED_METHODS);
    return firewall;
  }

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      VOMSAuthenticationProvider vomsProvider,
      StormJwtAuthenticationConverter authConverter)
      throws Exception {

    http.authenticationProvider(vomsProvider).addFilter(vomsFilter);

    if (serviceConfigurationProperties.getAuthz().isDisabled()) {
      LOG.warn("AUTHORIZATION DISABLED: this shouldn't be used in production!");
      http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
    } else {
      addAccessRules(http);
      addAnonymousAccessRules(http);
    }

    if (serviceConfigurationProperties.getRedirector().isEnabled()) {
      http.headers(headers -> headers.httpStrictTransportSecurity(HstsConfig::disable));
    }

    http.oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authConverter)));

    http.authorizeHttpRequests(
        authorize ->
            authorize.requestMatchers(AntPathRequestMatcher.antMatcher("/errors/**")).permitAll());

    http.authorizeHttpRequests(
        authorize ->
            authorize
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/"),
                    AntPathRequestMatcher.antMatcher("/robots.txt"),
                    AntPathRequestMatcher.antMatcher("/assets/css/*"),
                    AntPathRequestMatcher.antMatcher("/assets/js/*"),
                    AntPathRequestMatcher.antMatcher("/authn-info"),
                    AntPathRequestMatcher.antMatcher("/actuator/*"),
                    AntPathRequestMatcher.antMatcher("/status/metrics"),
                    AntPathRequestMatcher.antMatcher("/oauth/token"),
                    AntPathRequestMatcher.antMatcher("/.well-known/oauth-authorization-server"),
                    AntPathRequestMatcher.antMatcher("/.well-known/openid-configuration"),
                    AntPathRequestMatcher.antMatcher("/.well-known/wlcg-tape-rest-api"))
                .permitAll());

    configureOidcAuthn(http);

    if (!serviceConfigurationProperties.getAuthz().isDisabled()) {
      http.authorizeHttpRequests(
          authorize -> authorize.anyRequest().access(fineGrainedAuthorizationManager(null)));
    }

    AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
    handler.setErrorPage("/errors/403");
    http.exceptionHandling(
        exception ->
            exception.accessDeniedHandler(
                new SaveAuthnAccessDeniedHandler(principalHelper, handler)));

    http.logout(
        logout ->
            logout
                .logoutUrl("/logout")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/"));

    if (!oauthProperties.isEnableOidc()) {
      http.exceptionHandling(
          exception -> exception.authenticationEntryPoint(new ErrorPageAuthenticationEntryPoint()));
    }

    http.csrf(csrf -> csrf.disable());
    http.cors(cors -> cors.disable());

    return http.build();
  }

  @Bean
  static ErrorPageRegistrar securityErrorPageRegistrar() {
    return r -> {
      r.addErrorPages(new ErrorPage(RequestRejectedException.class, "/errors/400"));
      r.addErrorPages(new ErrorPage(InsufficientAuthenticationException.class, "/errors/401"));
      r.addErrorPages(new ErrorPage(BAD_REQUEST, "/errors/400"));
      r.addErrorPages(new ErrorPage(UNAUTHORIZED, "/errors/401"));
      r.addErrorPages(new ErrorPage(FORBIDDEN, "/errors/403"));
      r.addErrorPages(new ErrorPage(NOT_FOUND, "/errors/404"));
      r.addErrorPages(new ErrorPage(METHOD_NOT_ALLOWED, "/errors/405"));
    };
  }

  @Bean
  RequestRejectedHandler requestRejectedHandler() {
    return new HttpMethodRequestRejectedHandler(ALLOWED_METHODS);
  }

  protected void addAnonymousAccessRules(HttpSecurity http) throws Exception {
    final List<GrantedAuthority> anonymousAccessPermissions = new ArrayList<>();

    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {
      if (sa.anonymousReadEnabled()) {
        anonymousAccessPermissions.add(SAPermission.canRead(sa.name()));
      }
    }

    if (!anonymousAccessPermissions.isEmpty()) {
      http.anonymous(anonymous -> anonymous.authorities(anonymousAccessPermissions));
    }
  }

  protected void configureOidcAuthn(HttpSecurity http) throws Exception {
    if (oauthProperties.isEnableOidc()) {
      http.authorizeHttpRequests(
          authorize ->
              authorize
                  .requestMatchers(AntPathRequestMatcher.antMatcher("/oidc-login"))
                  .permitAll());
      http.oauth2Login(oauth2Login -> oauth2Login.loginPage("/oidc-login"));
    }
  }

  protected void addAccessRules(HttpSecurity http) throws Exception {

    Map<String, String> accessPoints = new TreeMap<>(Comparator.reverseOrder());
    saConfiguration
        .getStorageAreaInfo()
        .forEach(sa -> sa.accessPoints().forEach(ap -> accessPoints.put(ap, sa.name())));
    for (Entry<String, String> e : accessPoints.entrySet()) {
      String ap = e.getKey();
      String sa = e.getValue();
      LOG.debug("Evaluating access rules for access-point '{}' and storage area '{}'", ap, sa);
      String writeAccessRule =
          String.format(
              "hasAuthority('%s') and hasAuthority('%s')",
              SAPermission.canRead(sa).getAuthority(), SAPermission.canWrite(sa).getAuthority());
      LOG.debug("Write access rule: {}", writeAccessRule);
      String readAccessRule =
          String.format("hasAuthority('%s')", SAPermission.canRead(sa).getAuthority());
      LOG.debug("Read access rule: {}", readAccessRule);
      http.authorizeHttpRequests(
          authorize ->
              authorize
                  .requestMatchers(new ReadonlyHttpMethodMatcher(ap + "/**"))
                  .access(
                      fineGrainedAuthorizationManager(
                          new WebExpressionAuthorizationManager(readAccessRule))));

      http.authorizeHttpRequests(
          authorize ->
              authorize
                  .requestMatchers(AntPathRequestMatcher.antMatcher(ap + "/**"))
                  .access(
                      fineGrainedAuthorizationManager(
                          new WebExpressionAuthorizationManager(writeAccessRule))));
    }
  }

  protected AuthorizationManager<RequestAuthorizationContext> fineGrainedAuthorizationManager(
      WebExpressionAuthorizationManager webExpressionAuthorizationManager) {
    List<AuthorizationManager<RequestAuthorizationContext>> voters = new ArrayList<>();

    UnanimousDelegatedManager fineGrainedVoters =
        forVoters(
            "FineGrainedAuthz",
            asList(
                new FineGrainedAuthzManager(
                    serviceConfigurationProperties,
                    pathResolver,
                    fineGrainedAuthzPdp,
                    localURLService),
                new FineGrainedCopyMoveAuthzManager(
                    serviceConfigurationProperties,
                    pathResolver,
                    fineGrainedAuthzPdp,
                    localURLService)));

    WlcgStructuredPathAuthorizationPdp wlcgPdp =
        new WlcgStructuredPathAuthorizationPdp(
            serviceConfigurationProperties, pathResolver, localURLService);

    UnanimousDelegatedManager wlcgVoters =
        forVoters(
            "WLCGScopeBasedAuthz",
            asList(
                new WlcgScopeAuthzManager(
                    serviceConfigurationProperties, pathResolver, wlcgPdp, localURLService),
                new WlcgScopeAuthzCopyMoveManager(
                    serviceConfigurationProperties, pathResolver, wlcgPdp, localURLService)));

    if (serviceConfigurationProperties.getRedirector().isEnabled()) {
      try {
        voters.add(
            new LocalAuthzManager(
                serviceConfigurationProperties,
                pathResolver,
                new LocalAuthorizationPdp(serviceConfigurationProperties),
                localURLService));
      } catch (MalformedURLException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    if (serviceConfigurationProperties.getMacaroonFilter().isEnabled()) {
      voters.add(new MacaroonAuthzManager());
    }
    if (webExpressionAuthorizationManager != null) {
      voters.add(webExpressionAuthorizationManager);
    }
    voters.add(fineGrainedVoters);
    voters.add(wlcgVoters);
    return new ConsensusBasedManager("Consensus", voters);
  }
}
