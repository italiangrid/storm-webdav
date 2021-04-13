/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.test.utils.voms;

import static java.util.Objects.isNull;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.italiangrid.storm.webdav.authz.SAPermission;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationDetails;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.X509SubjectAuthority;
import org.italiangrid.voms.VOMSAttribute;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.google.common.collect.Lists;

public class VOMSSecurityContextBuilder {

  protected String subject;

  protected int acExpirationSecs;

  protected List<GrantedAuthority> authorities;

  final Clock clock;

  public VOMSSecurityContextBuilder(Clock clock) {
    this.clock = clock;
  }

  public VOMSSecurityContextBuilder acExpirationSecs(int secs) {
    acExpirationSecs = secs;
    return this;
  }

  public VOMSSecurityContextBuilder subject(String s) {
    subject = s;
    return this;
  }

  public VOMSSecurityContextBuilder vos(String... vos) {

    for (String vo : vos) {
      if (!isNull(authorities)) {
        authorities.add(new VOMSVOAuthority(vo));

      } else {
        authorities = Lists.newArrayList(new VOMSVOAuthority(vo));
      }
    }
    return this;
  }

  public VOMSSecurityContextBuilder saReadPermissions(String... sas) {
    for (String sa : sas) {
      if (!isNull(authorities)) {
        authorities.add(SAPermission.canRead(sa));
      } else {
        authorities = Lists.newArrayList(SAPermission.canRead(sa));
      }
    }
    return this;
  }

  public VOMSSecurityContextBuilder saWritePermissions(String... sas) {
    for (String sa : sas) {
      if (!isNull(authorities)) {
        authorities.add(SAPermission.canWrite(sa));
      } else {
        authorities = Lists.newArrayList(SAPermission.canWrite(sa));
      }
    }
    return this;
  }

  public SecurityContext buildSecurityContext() {
    SecurityContext context = SecurityContextHolder.createEmptyContext();

    authorities.add(new X509SubjectAuthority(subject));

    PreAuthenticatedAuthenticationToken token =
        new PreAuthenticatedAuthenticationToken(subject, new Object(), authorities);

    Instant expirationTime =
        clock.instant().plusSeconds(acExpirationSecs).truncatedTo(ChronoUnit.SECONDS);

    VOMSAttribute attrs = Mockito.mock(VOMSAttribute.class);
    VOMSAuthenticationDetails details = Mockito.mock(VOMSAuthenticationDetails.class);

    when(details.getVomsAttributes()).thenReturn(Lists.newArrayList(attrs));
    when(attrs.getNotAfter()).thenReturn(Date.from(expirationTime));

    token.setDetails(details);
    context.setAuthentication(token);
    return context;

  }
}
