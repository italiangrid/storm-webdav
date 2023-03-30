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
package org.italiangrid.storm.webdav.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class SecurityErrorController {

  @ResponseStatus(BAD_REQUEST)
  @GetMapping("/errors/400")
  String badRequestError(RequestRejectedException e) {
    return "400";
  }
  
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @GetMapping("/errors/401")
  String unauthorized() {
    return "401";
  }
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @GetMapping("/errors/403")
  String forbidden(Authentication auth, Model model) {
    return "403";
  }
  
  @ResponseStatus(NOT_FOUND)
  @GetMapping("/errors/404")
  String notFound() {
    return "404";
  }
  
  @ResponseStatus(METHOD_NOT_ALLOWED)
  @GetMapping("/errors/405")
  String methodNotAllowed() {
    return "405";
  }
}
