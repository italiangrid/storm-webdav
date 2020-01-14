/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import org.aeonbits.owner.Config;

public interface OwnerStorageAreaInfo extends StorageAreaInfo, Config {

  @Override
  @DefaultValue("posix")
  public String filesystemType();

  @Override
  @Separator(",")
  public List<String> accessPoints();

  @Override
  @Separator(",")
  public Set<String> vos();

  @Override
  @Separator(",")
  public Set<String> orgs();

  @DefaultValue("false")
  public Boolean anonymousReadEnabled();

  @Override
  @DefaultValue("false")
  public Boolean authenticatedReadEnabled();

  @Override
  @DefaultValue("true")
  public Boolean voMapEnabled();

  @Override
  @DefaultValue("false")
  public Boolean voMapGrantsWritePermission();

  @Override
  @DefaultValue("true")
  public Boolean orgsGrantReadPermission();

  @Override
  @DefaultValue("true")
  public Boolean orgsGrantWritePermission();

  @Override
  @DefaultValue("false")
  public Boolean wlcgStructuredScopeAuthzEnabled();
}
