package org.italiangrid.storm.webdav.oauth.authzserver.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthzServerMetadataController {

  private final AuthzServerMetadata metadata;

  @Autowired
  public AuthzServerMetadataController(AuthzServerMetadata md) {
    this.metadata = md;
  }

  @RequestMapping({".well-known/oauth-authorization-server", ".well-known/openid-configuration"})
  public AuthzServerMetadata getMetadata() {
    return metadata;
  }

}
