/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.italiangrid.storm.webdav.milton;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.italiangrid.storm.webdav.error.StoRMWebDAVError;

public class StoRMDirectoryResource extends StoRMResource implements
  PutableResource, MakeCollectionableResource, DeletableResource,
  CopyableResource {

  public StoRMDirectoryResource(StoRMResourceFactory factory, File f) {

    super(factory, f);
  }

  public File childrenFile(String filename) {

    return new File(getFile(), filename);
  }

  @Override
  public Resource child(String childName) throws NotAuthorizedException,
    BadRequestException {

    File child = new File(getFile(), childName);

    if (child.exists()) {
      if (child.isFile()) {
        return new StoRMFileResource(resourceFactory, child);
      } else if (child.isDirectory()) {
        return new StoRMDirectoryResource(resourceFactory, child);
      } else {
        throw new StoRMWebDAVError("Child file is not a file or directory: "
          + child.getAbsolutePath());
      }
    }

    return null;
  }

  @Override
  public List<? extends Resource> getChildren() throws NotAuthorizedException,
    BadRequestException {

    List<StoRMResource> childResources = new ArrayList<StoRMResource>();

    for (File f : file.listFiles()) {
      if (f.isDirectory()) {
        childResources.add(new StoRMDirectoryResource(getResourceFactory(), f));
      } else if (f.isFile()) {
        childResources.add(new StoRMFileResource(getResourceFactory(), f));
      }
    }

    return childResources;
  }

  @Override
  public CollectionResource createCollection(String dirName)
    throws NotAuthorizedException, ConflictException, BadRequestException {

    File nd = getFilesystemAccess().mkdir(getFile(), dirName);
    return new StoRMDirectoryResource(getResourceFactory(), nd);
  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
    BadRequestException {

    getFilesystemAccess().rm(getFile());

  }

  @Override
  public Resource createNew(String fileName, InputStream inputStream,
    Long length, String contentType) throws IOException, ConflictException,
    NotAuthorizedException, BadRequestException {

    File targetFile = new File(getFile(), fileName);
    getFilesystemAccess().create(targetFile, inputStream);

    return new StoRMFileResource(getResourceFactory(), targetFile);
  }

  @Override
  public void copyTo(CollectionResource toCollection, String name)
    throws NotAuthorizedException, BadRequestException, ConflictException {

    StoRMDirectoryResource dir = (StoRMDirectoryResource) toCollection;
    File destDir = dir.childrenFile(name);

    getFilesystemAccess().cp(getFile(), destDir);
  }

  @Override
  public String toString() {

    return "StoRMDirectoryResource [resourceFactory=" + resourceFactory
      + ", file=" + file + "]";
  }

}
