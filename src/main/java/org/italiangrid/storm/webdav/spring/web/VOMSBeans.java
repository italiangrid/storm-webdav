// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import eu.emi.security.authn.x509.X509CertChainValidatorExt;
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

@Configuration
public class VOMSBeans {

  @Bean
  VOMSACValidator vomsValidator(
      X509CertChainValidatorExt certificateValidator, ServiceConfigurationProperties props) {

    X509CertChainValidatorExt certVal = certificateValidator;

    if (props.getVoms().getCache().isEnabled()) {
      certVal =
          new CachingCertificateValidator(
              certificateValidator,
              TimeUnit.SECONDS.toMillis(props.getVoms().getCache().getEntryLifetimeSec()));
    }

    VOMSListener listener = new VOMSListener();

    VOMSTrustStore trustStore =
        VOMSTrustStores.newTrustStore(
            Arrays.asList(props.getVoms().getTrustStore().getDir()),
            TimeUnit.SECONDS.toMillis(props.getVoms().getTrustStore().getRefreshIntervalSec()),
            listener);

    return new DefaultVOMSValidator.Builder()
        .certChainValidator(certVal)
        .validationListener(listener)
        .trustStore(trustStore)
        .build();
  }

  @Bean
  VOMSAuthenticationProvider vomsAuthenticationProvider() {
    return new VOMSAuthenticationProvider();
  }

  @Bean
  VOMSPreAuthDetailsSource vomsDetailsSource(
      VOMSACValidator validator, AuthorizationPolicyService ps, VOMapDetailServiceBuilder builder) {
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
