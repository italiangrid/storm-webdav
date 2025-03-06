// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
  public boolean anonymousReadEnabled();

  @Override
  @DefaultValue("false")
  public boolean authenticatedReadEnabled();

  @Override
  @DefaultValue("true")
  public boolean voMapEnabled();

  @Override
  @DefaultValue("false")
  public boolean voMapGrantsWritePermission();

  @Override
  @DefaultValue("true")
  public boolean orgsGrantReadPermission();

  @Override
  @DefaultValue("true")
  public boolean orgsGrantWritePermission();

  @Override
  @DefaultValue("false")
  public boolean wlcgScopeAuthzEnabled();

  @Override
  @DefaultValue("false")
  public boolean fineGrainedAuthzEnabled();
}
