/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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

import static org.springframework.boot.autoconfigure.security.SecurityProperties.DEFAULT_FILTER_ORDER;

import java.time.Clock;

import org.italiangrid.storm.webdav.authn.PrincipalHelper;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.macaroon.MacaroonIssuerService;
import org.italiangrid.storm.webdav.macaroon.MacaroonRequestFilter;
import org.italiangrid.storm.webdav.metrics.StorageAreaStatsFilter;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.redirector.RedirectFilter;
import org.italiangrid.storm.webdav.redirector.RedirectionService;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.ChecksumFilter;
import org.italiangrid.storm.webdav.server.servlet.DeleteSanityChecksFilter;
import org.italiangrid.storm.webdav.server.servlet.LogRequestFilter;
import org.italiangrid.storm.webdav.server.servlet.MiltonFilter;
import org.italiangrid.storm.webdav.server.servlet.MoveRequestSanityChecksFilter;
import org.italiangrid.storm.webdav.server.servlet.SAIndexServlet;
import org.italiangrid.storm.webdav.server.servlet.SciTagFilter;
import org.italiangrid.storm.webdav.server.servlet.ServerResponseHeaderFilter;
import org.italiangrid.storm.webdav.server.servlet.StoRMServlet;
import org.italiangrid.storm.webdav.server.tracing.LogbackAccessAuthnInfoFilter;
import org.italiangrid.storm.webdav.server.tracing.RequestIdFilter;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TransferFilter;
import org.italiangrid.storm.webdav.tpc.http.HttpTransferClientMetricsWrapper;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.databind.ObjectMapper;



@Configuration
public class ServletConfiguration {

  public static final Logger LOG = LoggerFactory.getLogger(ServletConfiguration.class);

  static final int REQUEST_ID_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1000;
  static final int LOGBACK_ACCESS_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1001;
  static final int LOG_REQ_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1002;
  static final int REDIRECT_REQ_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1003;
  static final int CHECKSUM_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1004;
  static final int MACAROON_REQ_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1005;
  static final int SCITAG_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1006;
  static final int TPC_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1007;
  static final int MOVE_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1008;
  static final int DELETE_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1009;
  static final int MILTON_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1010;
  static final int SERVER_FILTER_ORDER = DEFAULT_FILTER_ORDER - 100;
  static final int STATS_FILTER_ORDER = DEFAULT_FILTER_ORDER - 200;

  @Bean
  FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
    FilterRegistrationBean<RequestIdFilter> requestIdFilter =
        new FilterRegistrationBean<>(new RequestIdFilter());

    requestIdFilter.addUrlPatterns("/*");
    requestIdFilter.setOrder(REQUEST_ID_FILTER_ORDER);

    return requestIdFilter;
  }

  @Bean
  FilterRegistrationBean<LogbackAccessAuthnInfoFilter> authnInfoFilter(PrincipalHelper helper) {
    FilterRegistrationBean<LogbackAccessAuthnInfoFilter> filter =
        new FilterRegistrationBean<>(new LogbackAccessAuthnInfoFilter(helper));

    filter.addUrlPatterns("/*");
    filter.setOrder(LOGBACK_ACCESS_FILTER_ORDER);

    return filter;
  }


  @Bean
  FilterRegistrationBean<LogRequestFilter> logRequestFilter() {
    FilterRegistrationBean<LogRequestFilter> logRequestFilter =
        new FilterRegistrationBean<>(new LogRequestFilter());

    logRequestFilter.addUrlPatterns("/*");
    logRequestFilter.setOrder(LOG_REQ_FILTER_ORDER);
    return logRequestFilter;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.redirector.enabled", havingValue = "true")
  FilterRegistrationBean<RedirectFilter> redirectFilter(PathResolver pathResolver,
      RedirectionService redirectionService) {
    LOG.info("Redirector filter enabled");

    FilterRegistrationBean<RedirectFilter> filter =
        new FilterRegistrationBean<>(new RedirectFilter(pathResolver, redirectionService));

    filter.addUrlPatterns("/*");
    filter.setOrder(REDIRECT_REQ_FILTER_ORDER);
    return filter;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.checksum-filter.enabled", havingValue = "true")
  FilterRegistrationBean<ChecksumFilter> checksumFilter(ExtendedAttributesHelper helper,
      PathResolver resolver) {
    LOG.info("Checksum filter enabled");
    FilterRegistrationBean<ChecksumFilter> filter =
        new FilterRegistrationBean<>(new ChecksumFilter(helper, resolver));

    filter.addUrlPatterns("/*");
    filter.setOrder(CHECKSUM_FILTER_ORDER);
    return filter;
  }

  @Bean
  @ConditionalOnExpression("${storm.macaroon-filter.enabled} && ${storm.authz-server.enabled}")
  FilterRegistrationBean<MacaroonRequestFilter> macaroonRequestFilter(ObjectMapper mapper,
      MacaroonIssuerService service) {
    LOG.info("Macaroon request filter enabled");
    FilterRegistrationBean<MacaroonRequestFilter> filter =
        new FilterRegistrationBean<>(new MacaroonRequestFilter(mapper, service));
    filter.setOrder(MACAROON_REQ_FILTER_ORDER);
    return filter;
  }

  @Bean
  @ConditionalOnProperty(name = "storm.scitags.enabled", havingValue = "true")
  FilterRegistrationBean<SciTagFilter> scitagFilter() {
    LOG.info("SciTag filter enabled");
    FilterRegistrationBean<SciTagFilter> filter = new FilterRegistrationBean<>(new SciTagFilter());
    filter.setOrder(SCITAG_FILTER_ORDER);
    return filter;
  }

  @Bean
  FilterRegistrationBean<MiltonFilter> miltonFilter(FilesystemAccess fsAccess,
      ExtendedAttributesHelper attrsHelper, PathResolver resolver, ReplaceContentStrategy rcs) {
    FilterRegistrationBean<MiltonFilter> miltonFilter =
        new FilterRegistrationBean<>(new MiltonFilter(fsAccess, attrsHelper, resolver, rcs));
    miltonFilter.addUrlPatterns("/*");
    miltonFilter.setOrder(MILTON_FILTER_ORDER);
    return miltonFilter;
  }


  @Bean
  FilterRegistrationBean<MoveRequestSanityChecksFilter> moveFilter(PathResolver resolver) {

    FilterRegistrationBean<MoveRequestSanityChecksFilter> moveFilter =
        new FilterRegistrationBean<>(new MoveRequestSanityChecksFilter(resolver));

    moveFilter.addUrlPatterns("/*");
    moveFilter.setOrder(MOVE_FILTER_ORDER);
    return moveFilter;
  }

  @Bean
  FilterRegistrationBean<DeleteSanityChecksFilter> deleteFilter(PathResolver resolver) {
    FilterRegistrationBean<DeleteSanityChecksFilter> deleteFilter =
        new FilterRegistrationBean<>(new DeleteSanityChecksFilter(resolver));
    deleteFilter.addUrlPatterns("/*");
    deleteFilter.setOrder(DELETE_FILTER_ORDER);
    return deleteFilter;
  }

  @Bean
  FilterRegistrationBean<TransferFilter> tpcFilter(Clock clock, FilesystemAccess fs,
      ExtendedAttributesHelper attrsHelper, PathResolver resolver, TransferClient client,
      ThirdPartyCopyProperties props, LocalURLService lus, MetricRegistry registry) {

    TransferClient metricsClient = new HttpTransferClientMetricsWrapper(registry, client);

    FilterRegistrationBean<TransferFilter> tpcFilter =
        new FilterRegistrationBean<>(new TransferFilter(clock, metricsClient, resolver, lus,
            props.isVerifyChecksum(), props.getEnableExpectContinueThreshold()));
    tpcFilter.addUrlPatterns("/*");
    tpcFilter.setOrder(TPC_FILTER_ORDER);
    return tpcFilter;
  }

  @Bean
  FilterRegistrationBean<StorageAreaStatsFilter> statsFilter(MetricRegistry registry,
      PathResolver resolver) {

    FilterRegistrationBean<StorageAreaStatsFilter> filter =
        new FilterRegistrationBean<>(new StorageAreaStatsFilter(registry, resolver));
    filter.addUrlPatterns("/*");
    filter.setOrder(STATS_FILTER_ORDER);
    return filter;
  }

  @Bean
  FilterRegistrationBean<ServerResponseHeaderFilter> serverHeaderFilter() {
    FilterRegistrationBean<ServerResponseHeaderFilter> filter =
        new FilterRegistrationBean<>(new ServerResponseHeaderFilter());
    filter.addUrlPatterns("/*");
    filter.setOrder(SERVER_FILTER_ORDER);
    return filter;
  }

  @Bean
  ServletRegistrationBean<MetricsServlet> metricsServlet(MetricRegistry registry) {
    ServletRegistrationBean<MetricsServlet> metricsServlet =
        new ServletRegistrationBean<>(new MetricsServlet(registry), "/status/metrics");
    metricsServlet.setAsyncSupported(false);
    return metricsServlet;
  }

  @Bean
  ServletRegistrationBean<StoRMServlet> stormServlet(OAuthProperties oauthProperties,
      ServiceConfigurationProperties serviceConfig, StorageAreaConfiguration saConfig,
      PathResolver pathResolver, TemplateEngine templateEngine) {

    ServletRegistrationBean<StoRMServlet> stormServlet = new ServletRegistrationBean<>(
        new StoRMServlet(oauthProperties, serviceConfig, pathResolver, templateEngine));

    stormServlet.addInitParameter("acceptRanges", "true");
    stormServlet.addInitParameter("dirAllowed", "true");
    stormServlet.addInitParameter("precompressed", "false");


    saConfig.getStorageAreaInfo()
      .forEach(i -> i.accessPoints().forEach(m -> stormServlet.addUrlMappings(m + "/*", m)));

    return stormServlet;
  }

  @Bean
  ServletRegistrationBean<SAIndexServlet> saIndexServlet(OAuthProperties oauthProperties,
      ServiceConfigurationProperties serviceConfig, StorageAreaConfiguration config,
      TemplateEngine engine) {
    return new ServletRegistrationBean<>(
        new SAIndexServlet(oauthProperties, serviceConfig, config, engine), "");
  }

}
