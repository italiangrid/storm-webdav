// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import java.io.File;
import java.util.Date;
import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;

public abstract class StoRMResource implements PropFindableResource, MoveableResource {

  protected final StoRMResourceFactory resourceFactory;
  protected final File file;

  protected StoRMResource(StoRMResourceFactory factory, File f) {

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
