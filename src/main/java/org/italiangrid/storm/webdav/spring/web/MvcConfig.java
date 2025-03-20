// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import java.util.concurrent.TimeUnit;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.web.PathConstants;
import org.italiangrid.storm.webdav.web.ViewUtilsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Autowired ServiceConfigurationProperties properties;

  @Autowired OAuthProperties oauthProperties;

  @Autowired StorageAreaConfiguration saConfig;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ViewUtilsInterceptor(properties, saConfig, oauthProperties));
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler(PathConstants.ASSETS_PATH + "/**")
        .addResourceLocations("classpath:/static/")
        .setCachePeriod((int) TimeUnit.HOURS.toSeconds(24));
  }
}
