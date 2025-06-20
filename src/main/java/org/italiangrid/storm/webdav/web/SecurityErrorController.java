// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(PathConstants.ERRORS_PATH)
public class SecurityErrorController {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @RequestMapping("/400")
  String badRequestError(RequestRejectedException e) {
    return "400";
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @RequestMapping("/401")
  String unauthorized() {
    return "401";
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @RequestMapping("/403")
  String forbidden(Authentication auth, Model model) {
    return "403";
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @RequestMapping("/404")
  String notFound() {
    return "404";
  }

  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  @RequestMapping("/405")
  String methodNotAllowed() {
    return "405";
  }

  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @RequestMapping("/415")
  String unsupportedMediaType() {
    return "415";
  }
}
