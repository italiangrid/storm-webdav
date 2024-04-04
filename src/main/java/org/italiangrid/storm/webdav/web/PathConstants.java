// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PathConstants {

  private static final PathConstants INSTANCE = new PathConstants();

  public static final String PATH_PREFIX = "/.storm-webdav";
  public static final String ACTUATOR_PATH = PATH_PREFIX + "/actuator";
  public static final String ASSETS_PATH = PATH_PREFIX + "/assets";
  public static final String AUTHN_INFO_PATH = PATH_PREFIX + "/authn-info";
  public static final String ERRORS_PATH = PATH_PREFIX + "/errors";
  public static final String INTERNAL_PATH = PATH_PREFIX + "/internal";
  // This is used in nginx X-Accel-Redirect header and should match the location in the
  // configuration
  public static final String INTERNAL_GET_PATH = INTERNAL_PATH + "/get";
  public static final String LOGOUT_PATH = PATH_PREFIX + "/logout";
  public static final String OAUTH_TOKEN_PATH = PATH_PREFIX + "/oauth/token";
  public static final String OIDC_LOGIN_PATH = PATH_PREFIX + "/oidc-login";

  @ModelAttribute("PathConstants")
  public static PathConstants pathConstants() {
    return INSTANCE;
  }

  private PathConstants() {}
}
