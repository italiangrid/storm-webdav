// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;
import java.util.Date;

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
  public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {

    return null;
  }
}
