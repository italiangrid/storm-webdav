// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.web;

import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthzServerMetadataController {

  private final AuthzServerMetadata metadata;

  public AuthzServerMetadataController(AuthzServerMetadata md) {
    this.metadata = md;
  }

  @GetMapping({".well-known/oauth-authorization-server", ".well-known/openid-configuration"})
  public ResponseEntity<AuthzServerMetadata> getMetadata() {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(6, TimeUnit.HOURS).cachePublic())
        .eTag(Integer.toHexString(metadata.hashCode()))
        .body(metadata);
  }
}
