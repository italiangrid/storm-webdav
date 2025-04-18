// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import io.opentelemetry.instrumentation.annotations.WithSpan;

public class ResourceAccessTokenRequest {

  public enum Permission {
    r,
    w,
    rw
  }

  final String path;
  final Permission permission;
  final String origin;
  final Integer lifetimeSecs;

  private ResourceAccessTokenRequest(
      String path, Permission permission, Integer lifetimeSecs, String origin) {
    this.path = path;
    this.permission = permission;
    this.lifetimeSecs = lifetimeSecs;
    this.origin = origin;
  }

  public String getPath() {
    return path;
  }

  public Permission getPermission() {
    return permission;
  }

  public Integer getLifetimeSecs() {
    return lifetimeSecs;
  }

  public String getOrigin() {
    return origin;
  }

  @WithSpan
  public static ResourceAccessTokenRequest forPath(
      String path, Permission permission, Integer lifetime, String origin) {
    return new ResourceAccessTokenRequest(path, permission, lifetime, origin);
  }
}
