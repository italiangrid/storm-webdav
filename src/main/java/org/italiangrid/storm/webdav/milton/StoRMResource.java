/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import java.io.File;
import java.util.Date;

import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.Resource;

public abstract class StoRMResource implements Resource, PropFindableResource, MoveableResource {

  protected final StoRMResourceFactory resourceFactory;
  protected final File file;

  public StoRMResource(StoRMResourceFactory factory, File f) {

    resourceFactory = factory;
    file = f;
  }

  @Override
  public Date getCreateDate() {

    return getModifiedDate();
  }

  @Override
  public String getUniqueId() {

    return file.getAbsolutePath();
  }

  @Override
  public String getName() {

    return file.getName();
  }

  @Override
  public Object authenticate(String user, String password) {

    return user;
  }

  @Override
  public boolean authorise(Request request, Method method, Auth auth) {

    return true;
  }

  @Override
  public String getRealm() {

    return null;
  }

  @Override
  public Date getModifiedDate() {

    return new Date(file.lastModified());
  }

  @Override
  public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {

    return null;
  }

  public File getFile() {

    return file;
  }

  public StoRMResourceFactory getResourceFactory() {

    return resourceFactory;
  }

  public FilesystemAccess getFilesystemAccess() {

    return resourceFactory.getFilesystemAccess();
  }

  public ExtendedAttributesHelper getExtendedAttributesHelper() {

    return resourceFactory.getExtendedAttributesHelper();
  }

  @Override
  public void moveTo(CollectionResource rDest, String name)
      throws ConflictException, NotAuthorizedException, BadRequestException {

    StoRMDirectoryResource dir = (StoRMDirectoryResource) rDest;
    getFilesystemAccess().mv(getFile(), dir.childrenFile(name));

  }

}
