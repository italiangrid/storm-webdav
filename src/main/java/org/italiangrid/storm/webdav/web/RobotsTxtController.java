// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotsTxtController {

  @GetMapping(path = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
  String getRobotsTxt() {
    return """
        User-agent: *
        Disallow: /
        """;
  }
}
