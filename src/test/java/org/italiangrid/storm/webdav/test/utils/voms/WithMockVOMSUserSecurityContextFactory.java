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
    return builder.subject(annotation.subject())
      .vos(annotation.vos())
      .acExpirationSecs(annotation.acExpirationSecs())
      .saReadPermissions(annotation.saReadPermissions())
      .saWritePermissions(annotation.saWritePermissions())
      .buildSecurityContext();
  }

}
