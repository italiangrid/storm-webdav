/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.italiangrid.storm.webdav.authz.VOMSAttributeCertificateAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.VOMSPreAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHTTPMethodMatcher;
import org.italiangrid.storm.webdav.authz.vomap.VOMapAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.storm.webdav.config.Constants;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.spring.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
// @Import(AppConfig.class)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  ServletContext context;

  @Autowired
  ServiceConfiguration serviceConfiguration;

  @Autowired
  VOMapDetailsService vomsMapDetailsService;

  private String getROVOAccessRule(Collection<String> vos) {

    StringBuilder accessRule = new StringBuilder();

    boolean first = true;
    accessRule.append("hasAnyRole(");

    for (String vo : vos) {

      if (!first) {
        accessRule.append(",");
      }

      VOMSVOAuthority voAuthority = new VOMSVOAuthority(vo);

      accessRule.append(String.format("'%s'", voAuthority));

      first = false;
    }

    accessRule.append(")");

    return accessRule.toString();
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
      vomsHelpers));
    return filter;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    VOMSAuthenticationProvider prov = new VOMSAuthenticationProvider();

    http.csrf().disable();

    http.authenticationProvider(prov).addFilter(
      buildVOMSAuthenticationFilter(prov));

    StorageAreaInfo sa = (StorageAreaInfo) context
      .getAttribute(Constants.SA_CONF_KEY);

    if (sa.anonymousReadEnabled() && sa.authenticatedReadEnabled()) {

      String accessRule = String
        .format("isAnonymous() or isAuthenticated() or %s",
          getROVOAccessRule(sa.vos()));

      http.authorizeRequests().requestMatchers(new ReadonlyHTTPMethodMatcher())
        .access(accessRule);

    } else if (sa.authenticatedReadEnabled()) {

      String accessRule = String.format("isAuthenticated() or %s",
        getROVOAccessRule(sa.vos()));

      http.authorizeRequests().requestMatchers(new ReadonlyHTTPMethodMatcher())
        .access(accessRule);

    }

    for (String vo : sa.vos()) {

      VOMSVOAuthority voAuthority = new VOMSVOAuthority(vo);

      http.authorizeRequests().antMatchers("**")
        .hasAuthority(voAuthority.getAuthority());
    }

  }

}
