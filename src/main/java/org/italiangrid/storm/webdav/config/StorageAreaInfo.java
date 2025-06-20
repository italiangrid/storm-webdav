// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import java.util.List;
import java.util.Set;

public interface StorageAreaInfo {

  String name();

  String rootPath();

  String filesystemType();

  List<String> accessPoints();

  Set<String> vos();

  Set<String> orgs();

  boolean anonymousReadEnabled();

  boolean authenticatedReadEnabled();

  boolean voMapEnabled();

  boolean voMapGrantsWritePermission();

  boolean orgsGrantReadPermission();

  boolean orgsGrantWritePermission();

  boolean wlcgScopeAuthzEnabled();

  boolean fineGrainedAuthzEnabled();

  boolean tapeEnabled();
}
