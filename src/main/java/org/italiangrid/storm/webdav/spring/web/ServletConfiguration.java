package org.italiangrid.storm.webdav.spring.web;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.DEFAULT_FILTER_ORDER;

import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.ThirdPartyCopyProperties;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.server.servlet.ChecksumFilter;
import org.italiangrid.storm.webdav.server.servlet.LogRequestFilter;
import org.italiangrid.storm.webdav.server.servlet.MiltonFilter;
import org.italiangrid.storm.webdav.server.servlet.SAIndexServlet;
import org.italiangrid.storm.webdav.server.servlet.StoRMServlet;
import org.italiangrid.storm.webdav.tpc.TransferFilter;
import org.italiangrid.storm.webdav.tpc.transfer.TransferClient;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;



@Configuration
public class ServletConfiguration {

  static final int LOG_REQ_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1000;
  static final int CHECKSUM_FILTER_ORDER = DEFAULT_FILTER_ORDER + 2000;
  static final int TPC_FILTER_ORDER = DEFAULT_FILTER_ORDER + 3000;
  static final int MILTON_FILTER_ORDER = DEFAULT_FILTER_ORDER + 4000;


  @Bean
  FilterRegistrationBean<LogRequestFilter> logRequestFilter() {
    FilterRegistrationBean<LogRequestFilter> logRequestFilter =
        new FilterRegistrationBean<LogRequestFilter>(new LogRequestFilter());

    logRequestFilter.addUrlPatterns("/*");
    logRequestFilter.setOrder(LOG_REQ_FILTER_ORDER);
    return logRequestFilter;
  }

  @Bean
  FilterRegistrationBean<ChecksumFilter> checksumFilter(ExtendedAttributesHelper helper,
      PathResolver resolver) {
    FilterRegistrationBean<ChecksumFilter> filter =
        new FilterRegistrationBean<>(new ChecksumFilter(helper, resolver));

    filter.addUrlPatterns("/*");
    filter.setOrder(CHECKSUM_FILTER_ORDER);
    return filter;
  }

  @Bean
  FilterRegistrationBean<MiltonFilter> miltonFilter(FilesystemAccess fsAccess,
      ExtendedAttributesHelper attrsHelper, PathResolver resolver) {
    FilterRegistrationBean<MiltonFilter> miltonFilter =
        new FilterRegistrationBean<>(new MiltonFilter(fsAccess, attrsHelper, resolver));
    miltonFilter.addUrlPatterns("/*");
    miltonFilter.setOrder(MILTON_FILTER_ORDER);
    return miltonFilter;
  }

  
  @Bean
  FilterRegistrationBean<TransferFilter> tpcFilter(FilesystemAccess fs,
      ExtendedAttributesHelper attrsHelper, PathResolver resolver, TransferClient client, ThirdPartyCopyProperties props) {
    FilterRegistrationBean<TransferFilter> tpcFilter = 
        new FilterRegistrationBean<>(new TransferFilter(client, resolver, props.isVerifyChecksum()));
    tpcFilter.addUrlPatterns("/*");
    tpcFilter.setOrder(TPC_FILTER_ORDER);
    return tpcFilter;
  }

  @Bean
  ServletRegistrationBean<MetricsServlet> metricsServlet(MetricRegistry registry) {
    ServletRegistrationBean<MetricsServlet> metricsServlet =
        new ServletRegistrationBean<>(new MetricsServlet(registry), "/status/metrics");
    metricsServlet.setAsyncSupported(false);
    return metricsServlet;
  }

  @Bean
  ServletRegistrationBean<StoRMServlet> stormServlet(StorageAreaConfiguration saConfig,
      PathResolver pathResolver) {

    ServletRegistrationBean<StoRMServlet> stormServlet =
        new ServletRegistrationBean<>(new StoRMServlet(pathResolver));

    stormServlet.addInitParameter("acceptRanges", "true");
    stormServlet.addInitParameter("dirAllowed", "true");
    stormServlet.addInitParameter("aliases", "false");
    stormServlet.addInitParameter("gzip", "false");

    saConfig.getStorageAreaInfo().forEach(i -> {
      i.accessPoints().forEach(m -> stormServlet.addUrlMappings(m + "/*", m));
    });

    return stormServlet;
  }

  @Bean
  ServletRegistrationBean<SAIndexServlet> saIndexServlet(StorageAreaConfiguration config,
      TemplateEngine engine) {
    return new ServletRegistrationBean<>(new SAIndexServlet(config, engine), "");
  }

}
