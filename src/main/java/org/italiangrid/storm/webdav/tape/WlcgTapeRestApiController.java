// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tape;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.italiangrid.storm.webdav.tape.model.WlcgTapeRestApi;
import org.italiangrid.storm.webdav.tape.service.WlcgTapeRestApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WlcgTapeRestApiController {

  private final WlcgTapeRestApiService service;

  public WlcgTapeRestApiController(WlcgTapeRestApiService service) {
    this.service = service;
  }

  @GetMapping({".well-known/wlcg-tape-rest-api"})
  public WlcgTapeRestApi getMetadata() {

    WlcgTapeRestApi metadata = service.getMetadata();
    if (metadata == null) {
      throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
    }
    return metadata;
  }
}
