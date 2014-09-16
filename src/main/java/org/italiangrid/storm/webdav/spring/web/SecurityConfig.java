/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.italiangrid.storm.webdav.spring.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.italiangrid.storm.webdav.authz.CopyMoveAuthzVoter;
import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.authz.VOMSAttributeCertificateAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.VOMSPreAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHTTPMethodMatcher;
import org.italiangrid.storm.webdav.authz.vomap.VOMapAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebExpressionVoter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  ServletContext context;

  @Autowired
  ServiceConfiguration serviceConfiguration;

  @Autowired
  StorageAreaConfiguration saConfiguration;

  @Autowired
  VOMapDetailsService vomsMapDetailsService;

  @Bean
  public AccessDecisionVoter<FilterInvocation> customVoter() {

    return new CopyMoveAuthzVoter(saConfiguration);
  }

  @Bean
  public AccessDecisionManager accessDecisionManager() {

    @SuppressWarnings("rawtypes")
    List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();
    voters.add(new WebExpressionVoter());
    voters.add(customVoter());

    return new UnanimousBased(voters);
  }

  protected VOMSAuthenticationFilter buildVOMSAuthenticationFilter(
    VOMSAuthenticationProvider provider) {

    List<VOMSAuthDetailsSource> vomsHelpers = new ArrayList<VOMSAuthDetailsSource>();

    vomsHelpers.add(new VOMSAttributeCertificateAuthDetailsSource());

    if (serviceConfiguration.enableVOMapFiles()
      && vomsMapDetailsService != null) {

      vomsHelpers.add(new VOMapAuthDetailsSource(vomsMapDetailsService));

    }

    VOMSAuthenticationFilter filter = new VOMSAuthenticationFilter(provider);
    filter.setAuthenticationDetailsSource(new VOMSPreAuthDetailsSource(
      vomsHelpers, saConfiguration));
    return filter;
  }

  protected void addAccessRules(HttpSecurity http) throws Exception {
    
    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {

      for (String ap : sa.accessPoints()) {

        String writeAccessRule = String.format(
          "hasRole('%s') and hasRole('%s')", SAPermission.canRead(sa.name())
            .getAuthority(), SAPermission.canWrite(sa.name()).getAuthority());

        http.authorizeRequests()
          .requestMatchers(new ReadonlyHTTPMethodMatcher(ap + "/**"))
          .hasAuthority(SAPermission.canRead(sa.name()).getAuthority());

        http.authorizeRequests().antMatchers(ap + "/**")
          .access(writeAccessRule);
      }
    }
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    final List<GrantedAuthority> anonymousAccessPermissions = 
      new ArrayList<GrantedAuthority>();

    for (StorageAreaInfo sa : saConfiguration.getStorageAreaInfo()) {

      if (sa.anonymousReadEnabled()) {

        anonymousAccessPermissions.add(SAPermission.canRead(sa.name()));
      }
    }

    VOMSAuthenticationProvider prov = new VOMSAuthenticationProvider();

    http.csrf().disable();

    http.authorizeRequests().accessDecisionManager(accessDecisionManager());

    http.authenticationProvider(prov).addFilter(
      buildVOMSAuthenticationFilter(prov));

    http.anonymous().authorities(anonymousAccessPermissions);

    addAccessRules(http);

  }

}
