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
