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

package org.italiangrid.storm.webdav.tape.model;

import java.util.List;

import com.google.common.collect.Lists;

public class WlcgTapeRestApi {

  private String sitename;
  private String description;
  private List<WlcgTapeRestApiEndpoint> endpoints = Lists.newArrayList();

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
