// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import java.util.List;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConditionalOnProperty(name = "oauth.enable-oidc", havingValue = "true")
public class OidcLoginController {

  final List<OidcClientModel> clients;
  final ServiceConfigurationProperties serviceProperties;

  @Autowired
  public OidcLoginController(
      OAuth2ClientProperties clientProperties, ServiceConfigurationProperties serviceProperties) {
    this.serviceProperties = serviceProperties;

    clients =
        clientProperties.getRegistration().entrySet().stream()
            .map(
                e -> {
                  Provider provider =
                      clientProperties.getProvider().get(e.getValue().getProvider());
                  OidcClientModel m = new OidcClientModel();
                  m.setName(e.getValue().getClientName());
                  m.setIssuer(provider.getIssuerUri());
                  m.setUrl(String.format("/oauth2/authorization/%s", e.getKey()));
                  return m;
                })
            .toList();
  }

  @GetMapping("/oidc-login")
  String oidcLoginController(Model model) {
    model.addAttribute("clients", clients);
    return "oidc-login";
  }

  public static class OidcClientModel {
    String name;
    String issuer;
    String url;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getIssuer() {
      return issuer;
    }

    public void setIssuer(String issuer) {
      this.issuer = issuer;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    @Override
    public String toString() {
      return "OidcClientModel [name=" + name + ", issuer=" + issuer + ", url=" + url + "]";
    }
  }
}
