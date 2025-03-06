// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils.voms;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockVOMSUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockVOMSUser> {

  final Clock clock;

  @Autowired
  public WithMockVOMSUserSecurityContextFactory(Clock clock) {
    this.clock = clock;
  }

  @Override
  public SecurityContext createSecurityContext(WithMockVOMSUser annotation) {

    VOMSSecurityContextBuilder builder = new VOMSSecurityContextBuilder(clock);
    return builder
        .subject(annotation.subject())
        .vos(annotation.vos())
        .acExpirationSecs(annotation.acExpirationSecs())
        .saReadPermissions(annotation.saReadPermissions())
        .saWritePermissions(annotation.saWritePermissions())
        .buildSecurityContext();
  }
}
