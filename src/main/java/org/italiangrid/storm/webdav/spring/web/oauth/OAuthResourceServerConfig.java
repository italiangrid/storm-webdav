package org.italiangrid.storm.webdav.spring.web.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;


public class OAuthResourceServerConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  JwtDecoder decoder;


  @Autowired
  JwtAuthenticationConverter authConverter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.oauth2ResourceServer().jwt().decoder(decoder).jwtAuthenticationConverter(authConverter);
  }

}
