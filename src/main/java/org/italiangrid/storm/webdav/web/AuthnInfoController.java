// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import org.italiangrid.storm.webdav.server.servlet.SAIndexServlet;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthnInfoController {

  @GetMapping(PathConstants.AUTHN_INFO_PATH)
  String getAuthenticationInfo(Authentication authentication, Model model) {
    if (authentication != null) {
      model.addAttribute(
          SAIndexServlet.AUTHN_CLASS_SIMPLE_NAME_KEY, authentication.getClass().getSimpleName());
    }
    return "authn-info";
  }
}
