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
package org.italiangrid.storm.webdav.config;

import java.util.List;
import java.util.Set;

public interface StorageAreaInfo {

  public String name();

  public String rootPath();

  public String filesystemType();

  public List<String> accessPoints();

  public Set<String> vos();

  public Set<String> orgs();

  public Boolean anonymousReadEnabled();

  public Boolean authenticatedReadEnabled();

  public Boolean voMapEnabled();

  public Boolean voMapGrantsWritePermission();

  public Boolean orgsGrantReadPermission();

  public Boolean orgsGrantWritePermission();

  public Boolean wlcgScopeAuthzEnabled();
  
  public Boolean fineGrainedAuthzEnabled();

  public Boolean isTapeEnabled();
}
