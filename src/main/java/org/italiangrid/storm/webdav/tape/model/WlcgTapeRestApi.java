// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tape.model;

import java.util.ArrayList;
import java.util.List;

public class WlcgTapeRestApi {

  private String sitename;
  private String description;
  private List<WlcgTapeRestApiEndpoint> endpoints = new ArrayList<>();

  public String getSitename() {
    return sitename;
  }

  public void setSitename(String sitename) {
    this.sitename = sitename;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<WlcgTapeRestApiEndpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<WlcgTapeRestApiEndpoint> endpoints) {
    this.endpoints = endpoints;
  }
}
