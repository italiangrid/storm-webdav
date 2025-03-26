// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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

  public boolean anonymousReadEnabled();

  public boolean authenticatedReadEnabled();

  public boolean voMapEnabled();

  public boolean voMapGrantsWritePermission();

  public boolean orgsGrantReadPermission();

  public boolean orgsGrantWritePermission();

  public boolean wlcgScopeAuthzEnabled();

  public boolean fineGrainedAuthzEnabled();

  public boolean tapeEnabled();
}
