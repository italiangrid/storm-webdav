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
package org.italiangrid.storm.webdav.oauth.authzserver;

public class ResourceAccessTokenRequest {

  public enum Permission {
    r,
    w,
    rw
  };

  final String path;
  final Permission permission;
  final String origin;
  final Integer lifetimeSecs;

  private ResourceAccessTokenRequest(String path, Permission permission, Integer lifetimeSecs,
      String origin) {
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

  public static ResourceAccessTokenRequest forPath(String path, Permission permission,
      Integer lifetime, String origin) {
    return new ResourceAccessTokenRequest(path, permission, lifetime, origin);
  }
}
