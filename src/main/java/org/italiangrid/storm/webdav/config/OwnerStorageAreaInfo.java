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
  String filesystemType();

  @Override
  @Separator(",")
  List<String> accessPoints();

  @Override
  @Separator(",")
  Set<String> vos();

  @Override
  @Separator(",")
  Set<String> orgs();

  @DefaultValue("false")
  boolean anonymousReadEnabled();

  @Override
  @DefaultValue("false")
  boolean authenticatedReadEnabled();

  @Override
  @DefaultValue("true")
  boolean voMapEnabled();

  @Override
  @DefaultValue("false")
  boolean voMapGrantsWritePermission();

  @Override
  @DefaultValue("true")
  boolean orgsGrantReadPermission();

  @Override
  @DefaultValue("true")
  boolean orgsGrantWritePermission();

  @Override
  @DefaultValue("false")
  boolean wlcgScopeAuthzEnabled();

  @Override
  @DefaultValue("false")
  boolean fineGrainedAuthzEnabled();

  @Override
  @DefaultValue("false")
  boolean tapeEnabled();
}
