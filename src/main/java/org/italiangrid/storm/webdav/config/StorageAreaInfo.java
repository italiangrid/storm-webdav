// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.springframework.util.Assert;

public record StorageAreaInfo(
    String name,
    String rootPath,
    String filesystemType,
    List<String> accessPoints,
    Set<String> vos,
    Set<String> orgs,
    boolean anonymousReadEnabled,
    boolean authenticatedReadEnabled,
    boolean voMapEnabled,
    boolean voMapGrantsWritePermission,
    boolean orgsGrantReadPermission,
    boolean orgsGrantWritePermission,
    boolean wlcgScopeAuthzEnabled,
    boolean fineGrainedAuthzEnabled,
    boolean tapeEnabled) {
  public StorageAreaInfo {
    Assert.hasText(name, "SA name must not be empty");
    Assert.hasText(rootPath, "SA rootPath must not be empty");
    Assert.notEmpty(accessPoints, "SA accessPoints must not be empty");
    if (vos == null) {
      vos = Set.of();
    }
    if (orgs == null) {
      orgs = Set.of();
    }
  }

  public StorageAreaInfo(Properties p) {
    this(
        p.getProperty("name"),
        p.getProperty("rootPath"),
        p.getProperty("filesystemType", "posix"),
        List.of(p.getProperty("accessPoints", "").split(",")),
        Set.of(p.getProperty("vos", "").split(",")),
        Set.of(p.getProperty("orgs", "").split(",")),
        Boolean.parseBoolean(p.getProperty("anonymousReadEnabled", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("authenticatedReadEnabled", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("voMapEnabled", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("voMapGrantsWritePermission", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("orgsGrantReadPermission", Boolean.TRUE.toString())),
        Boolean.parseBoolean(p.getProperty("orgsGrantWritePermission", Boolean.TRUE.toString())),
        Boolean.parseBoolean(p.getProperty("wlcgScopeAuthzEnabled", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("fineGrainedAuthzEnabled", Boolean.FALSE.toString())),
        Boolean.parseBoolean(p.getProperty("tapeEnabled", Boolean.FALSE.toString())));
  }
}
