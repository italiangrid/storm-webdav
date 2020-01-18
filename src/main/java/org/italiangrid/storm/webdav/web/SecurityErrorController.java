/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class SecurityErrorController {

  @ResponseStatus(BAD_REQUEST)
  @RequestMapping("/errors/400")
  String badRequestError(RequestRejectedException e) {
    return "errors/400";
  }
  
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @RequestMapping("/errors/401")
  String unauthorized() {
    return "errors/401";
  }
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @RequestMapping("/errors/403")
  String forbidden() {
    return "errors/403";
  }
  
  @ResponseStatus(NOT_FOUND)
  @RequestMapping("/errors/404")
  String notFound() {
    return "errors/404";
  }
  
  @ResponseStatus(METHOD_NOT_ALLOWED)
  @RequestMapping("/errors/405")
  String methodNotAllowed() {
    return "errors/405";
  }
}
