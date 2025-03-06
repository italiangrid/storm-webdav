// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class StormJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private final StormJwtAuthoritiesConverter converter;

  @Autowired
  public StormJwtAuthenticationConverter(StormJwtAuthoritiesConverter converter) {
    this.converter = converter;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    Collection<GrantedAuthority> authorities = converter.convert(source);
    return new JwtAuthenticationToken(source, authorities);
  }
}
