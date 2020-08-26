/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.spring.web;

import static java.util.Arrays.asList;
import static org.italiangrid.storm.webdav.authz.voters.UnanimousDelegatedVoter.forVoters;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.italiangrid.storm.webdav.authn.ErrorPageAuthenticationEntryPoint;
import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.WlcgStructuredPathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHttpMethodMatcher;
import org.italiangrid.storm.webdav.authz.voters.FineGrainedAuthzVoter;
import org.italiangrid.storm.webdav.authz.voters.FineGrainedCopyMoveAuthzVoter;
import org.italiangrid.storm.webdav.authz.voters.UnanimousDelegatedVoter;
import org.italiangrid.storm.webdav.authz.voters.WlcgScopeAuthzCopyMoveVoter;
import org.italiangrid.storm.webdav.authz.voters.WlcgScopeAuthzVoter;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.context.ServletContextAware;

import com.google.common.collect.Lists;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ServletContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

  ServletContext context;

  @Autowired
  ServiceConfiguration serviceConfiguration;

  @Autowired
  ServiceConfigurationProperties serviceConfigurationProperties;

  @Autowired
  StorageAreaConfiguration saConfiguration;

  @Autowired
  PathResolver pathResolver;

  @Autowired
  StormJwtAuthenticationConverter authConverter;

  @Autowired
  OAuthProperties oauthProperties;

  @Autowired
  VOMSAuthenticationProvider vomsProvider;

  @Autowired
  @Qualifier("vomsAuthenticationFilter")
  VOMSAuthenticationFilter vomsFilter;

  @Autowired
  LocalURLService localURLService;

  @Autowired
  PathAuthorizationPdp fineGrainedAuthzPdp;

  @Bean
  public static ErrorPageRegistrar securityErrorPageRegistrar() {
    return registry -> registry.addErrorPages(
        new ErrorPage(RequestRejectedException.class, "/errors/400"),
        new ErrorPage(InsufficientAuthenticationException.class, "/errors/401"),
        new ErrorPage(HttpStatus.BAD_REQUEST, "/errors/400"),
        new ErrorPage(HttpStatus.UNAUTHORIZED, "/errors/401"),
        new ErrorPage(HttpStatus.FORBIDDEN, "/errors/403"),
        new ErrorPage(HttpStatus.NOT_FOUND, "/errors/404"),
        new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/errors/405"));

  }

  @Bean
  public HttpFirewall allowWebDAVMethodsFirewall() {

    StrictHttpFirewall firewall = new StrictHttpFirewall();
    List<String> allowedMethods = Lists.newArrayList();

    for (HttpMethod m : HttpMethod.values()) {
      allowedMethods.add(m.name());
    }

    for (WebDAVMethod m : WebDAVMethod.values()) {
      allowedMethods.add(m.name());
    }

    firewall.setAllowedHttpMethods(allowedMethods);
    return firewall;
  }

  public AccessDecisionManager fineGrainedAccessDecisionManager() {
    List<AccessDecisionVoter<?>> voters = new ArrayList<>();

    UnanimousDelegatedVoter fineGrainedVoters = forVoters("FineGrainedAuthz", asList(
        new FineGrainedAuthzVoter(serviceConfigurationProperties, pathResolver, fineGrainedAuthzPdp,
            localURLService),
        new FineGrainedCopyMoveAuthzVoter(serviceConfigurationProperties, pathResolver,
            fineGrainedAuthzPdp, localURLService)));

    WlcgStructuredPathAuthorizationPdp wlcgPdp = new WlcgStructuredPathAuthorizationPdp(
        serviceConfigurationProperties, pathResolver, localURLService);

    UnanimousDelegatedVoter wlcgVoters = forVoters("WLCGScopeBasedAuthz", asList(
        new WlcgScopeAuthzVoter(serviceConfigurationProperties, pathResolver, wlcgPdp,
            localURLService),
        new WlcgScopeAuthzCopyMoveVoter(serviceConfigurationProperties, pathResolver, wlcgPdp,
            localURLService)));


    voters.add(new WebExpressionVoter());
    voters.add(fineGrainedVoters);
    voters.add(wlcgVoters);
    return new ConsensusBased(voters);
  }

  protected void addAccessRules(HttpSecurity http) throws Exception {

    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {

      for (String ap : sa.accessPoints()) {

        String writeAccessRule = String.format("hasAuthority('%s') and hasAuthority('%s')",
            SAPermission.canRead(sa.name()).getAuthority(),
            SAPermission.canWrite(sa.name()).getAuthority());

        String readAccessRule =
            String.format("hasAuthority('%s')", SAPermission.canRead(sa.name()).getAuthority());

        http.authorizeRequests()
          .requestMatchers(new ReadonlyHttpMethodMatcher(ap + "/**"))
          .access(readAccessRule);

        http.authorizeRequests().antMatchers(ap + "/**").access(writeAccessRule);
      }
    }
  }

  protected void addAnonymousAccessRules(HttpSecurity http) throws Exception {
    final List<GrantedAuthority> anonymousAccessPermissions = new ArrayList<>();

    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {
      if (Boolean.TRUE.equals(sa.anonymousReadEnabled())) {
        anonymousAccessPermissions.add(SAPermission.canRead(sa.name()));
      }
    }

    if (!anonymousAccessPermissions.isEmpty()) {
      http.anonymous().authorities(anonymousAccessPermissions);
    }
  }



  protected void configureOidcAuthn(HttpSecurity http) throws Exception {
    if (oauthProperties.isEnableOidc()) {
      http.oauth2Login().loginPage("/oidc-login");
    }
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.csrf().disable();
    http.authenticationProvider(vomsProvider).addFilter(vomsFilter);

    if (serviceConfigurationProperties.getAuthz().isDisabled()) {
      LOG.warn("AUTHORIZATION DISABLED: this shouldn't be used in production!");
      http.authorizeRequests().anyRequest().permitAll();
    } else {
      http.authorizeRequests().accessDecisionManager(fineGrainedAccessDecisionManager());
      addAccessRules(http);
      addAnonymousAccessRules(http);
    }

    http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(authConverter);

    http.authorizeRequests().antMatchers(HttpMethod.GET, "/errors/**").permitAll();

    http.authorizeRequests()
      .antMatchers(HttpMethod.GET, "/.well-known/oauth-authorization-server",
          "/.well-known/openid-configuration")
      .permitAll();

    http.exceptionHandling().accessDeniedPage("/errors/403");
    http.logout()
      .logoutUrl("/logout")
      .clearAuthentication(true)
      .invalidateHttpSession(true)
      .logoutSuccessUrl("/");

    if (!oauthProperties.isEnableOidc()) {
      http.exceptionHandling().authenticationEntryPoint(new ErrorPageAuthenticationEntryPoint());
    }

    configureOidcAuthn(http);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    context = servletContext;
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/css/*", "/js/*");
  }

}
