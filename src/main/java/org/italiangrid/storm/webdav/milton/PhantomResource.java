/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
package org.italiangrid.storm.webdav.milton;

import java.util.Date;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;

public class PhantomResource implements Resource {

  String path;

  public PhantomResource(String path) {

    this.path = path;
  }

  @Override
  public String getUniqueId() {

    return null;
  }

  @Override
  public String getName() {

    return path;
  }

  @Override
  public Object authenticate(String user, String password) {

    return null;
  }

  @Override
  public boolean authorise(Request request, Method method, Auth auth) {

    return false;
  }

  @Override
  public String getRealm() {

    return null;
  }

  @Override
  public Date getModifiedDate() {

    return new Date();
  }

  @Override
  public String checkRedirect(Request request) throws NotAuthorizedException,
    BadRequestException {

    return null;
  }

}
