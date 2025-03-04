// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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

  private static final String LOG_INFO_LOADING = "Loading WLCG Tape REST API well-known endpoint from file '{}' ...";
  private static final String LOG_ERROR_PREFIX = "Error loading WLCG Tape REST API well-known endpoint from file: {}";
  private static final String LOG_INFO_NOFILEFOUND = "No WLCG Tape REST API well-known file found at '{}'";

  private WlcgTapeRestApi metadata;

  public WlcgTapeRestApiService(ServiceConfigurationProperties props) {

    metadata = null;
    File source = new File(props.getTape().getWellKnown().getSource());
    if (source.exists()) {
      LOG.info(LOG_INFO_LOADING, source);
      try {
        metadata = (new ObjectMapper()).readValue(source, WlcgTapeRestApi.class);
      } catch (IOException e) {
        LOG.error(LOG_ERROR_PREFIX, e.getMessage());
      }
    } else {
      LOG.info(LOG_INFO_NOFILEFOUND, source);
    }
  }

  public WlcgTapeRestApi getMetadata() {
    return metadata;
  }

}
