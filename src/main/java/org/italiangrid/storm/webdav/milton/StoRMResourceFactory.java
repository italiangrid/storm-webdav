/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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

import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.util.ReplaceContentStrategy;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;

public class StoRMResourceFactory implements ResourceFactory {

  public static final Logger LOG = LoggerFactory.getLogger(StoRMResourceFactory.class);

  private final FilesystemAccess fs;

  private final ExtendedAttributesHelper attrsHelper;

  private final PathResolver resolver;

  private final ReplaceContentStrategy rcs;

  public StoRMResourceFactory(FilesystemAccess fs, ExtendedAttributesHelper attrsHelper,
      PathResolver resolver, ReplaceContentStrategy rcs) {

    this.fs = fs;
    this.resolver = resolver;
    this.attrsHelper = attrsHelper;
    this.rcs = rcs;
  }


  @Override
  public Resource getResource(String host, String path)
      throws NotAuthorizedException, BadRequestException {

    String resolvedPath = resolver.resolvePath(path);

    if (resolvedPath == null) {
      return null;
    }

    File requestedFile = new File(resolvedPath);

    LOG.debug("getResource: path={}, resolvedPath={}", path, requestedFile.getAbsolutePath());

    if (!requestedFile.exists()) {
      LOG.debug(
          "Requested file '{}' does not exists or user {} does not have the rights to read it.",
          requestedFile, System.getProperty("user.name"));
      return null;
    }

    if (requestedFile.isDirectory()) {
      return new StoRMDirectoryResource(this, requestedFile);
    }

    return new StoRMFileResource(this, requestedFile);
  }

  public FilesystemAccess getFilesystemAccess() {

    return fs;
  }

  public ExtendedAttributesHelper getExtendedAttributesHelper() {

    return attrsHelper;
  }
  
  public ReplaceContentStrategy getReplaceContentStrategy() {
    return rcs;
  }

}
