// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tape;

import java.util.concurrent.TimeUnit;
import org.italiangrid.storm.webdav.tape.model.WlcgTapeRestApi;
import org.italiangrid.storm.webdav.tape.service.WlcgTapeRestApiService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<WlcgTapeRestApi> getMetadata() {

    WlcgTapeRestApi metadata = service.getMetadata();
    if (metadata == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(6, TimeUnit.HOURS).cachePublic())
        .eTag(Integer.toHexString(metadata.hashCode()))
        .body(metadata);
  }
}
