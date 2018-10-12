/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.italiangrid.storm.webdav.authz.AuthorizationPolicyService;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.VOMSPreAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailServiceBuilder;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.server.util.VOMSListener;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
import org.italiangrid.voms.store.VOMSTrustStore;
import org.italiangrid.voms.store.VOMSTrustStores;
import org.italiangrid.voms.util.CachingCertificateValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;

@Configuration
public class VOMSSecurityConfig {

  @Bean
  VOMSACValidator vomsValidator(X509CertChainValidatorExt certificateValidator,
      ServiceConfigurationProperties props) {

    X509CertChainValidatorExt certVal = certificateValidator;

    if (props.getVoms().getCache().isEnabled()) {
      certVal = new CachingCertificateValidator(certificateValidator,
          TimeUnit.SECONDS.toMillis(props.getVoms().getCache().getEntryLifetimeSec()));
    }

    VOMSListener listener = new VOMSListener();

    VOMSTrustStore trustStore =
        VOMSTrustStores.newTrustStore(Arrays.asList(props.getVoms().getTrustStore().getDir()),
            TimeUnit.SECONDS.toMillis(props.getVoms().getTrustStore().getRefreshIntervalSec()),
            listener);

    return new DefaultVOMSValidator.Builder().certChainValidator(certVal)
      .validationListener(listener)
      .trustStore(trustStore)
      .build();
  }

  @Bean
  VOMSAuthenticationProvider vomsAuthenticationProvider() {
    return new VOMSAuthenticationProvider();
  }

  @Bean
  VOMSPreAuthDetailsSource vomsDetailsSource(VOMSACValidator validator,
      AuthorizationPolicyService ps, VOMapDetailServiceBuilder builder) {
    return new VOMSPreAuthDetailsSource(validator, ps, builder.build());
  }

  @Bean
  VOMSAuthenticationFilter vomsAuthenticationFilter(VOMSPreAuthDetailsSource ds) {
    VOMSAuthenticationFilter filter = new VOMSAuthenticationFilter(vomsAuthenticationProvider());
    filter.setAuthenticationDetailsSource(ds);
    return filter;
  }


  @Bean
  FilterRegistrationBean<VOMSAuthenticationFilter> doNotRegisterVomsAuthenticationFilter(
      @Qualifier("vomsAuthenticationFilter") VOMSAuthenticationFilter filter) {

    FilterRegistrationBean<VOMSAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
    reg.setEnabled(false);

    return reg;
  }
}
