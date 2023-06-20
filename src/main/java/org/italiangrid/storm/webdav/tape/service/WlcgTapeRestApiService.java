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

package org.italiangrid.storm.webdav.tape.service;

import java.io.File;
import java.io.IOException;

import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.tape.model.WlcgTapeRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WlcgTapeRestApiService {

  public static final Logger LOG = LoggerFactory.getLogger(WlcgTapeRestApiService.class);

  private WlcgTapeRestApi metadata;

  public WlcgTapeRestApiService(ServiceConfigurationProperties props) {

    metadata = null;
    File source = new File(props.getTape().getWellKnown().getSource());
    if (source.exists()) {
      LOG.info("Loading WLCG Tape REST API well-known endpoint from file '{}'", source);
      try {
        metadata = (new ObjectMapper()).readValue(source, WlcgTapeRestApi.class);
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    } else {
      LOG.info("No WLCG Tape REST API well-known file found at '{}'", source);
    }
  }

  public WlcgTapeRestApi getMetadata() {
    return metadata;
  }

}
