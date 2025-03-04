// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthzServerMetadataController {

  private final AuthzServerMetadata metadata;

  public AuthzServerMetadataController(AuthzServerMetadata md) {
    this.metadata = md;
  }

  @GetMapping({".well-known/oauth-authorization-server", ".well-known/openid-configuration"})
  public AuthzServerMetadata getMetadata() {
    return metadata;
  }

}
