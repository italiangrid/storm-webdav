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
package org.italiangrid.storm.webdav.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoRMServlet extends DefaultServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 4204673943980786498L;

  private static final Logger logger = LoggerFactory
    .getLogger(StoRMServlet.class);

  final PathResolver pathResolver;
  final ExtendedAttributesHelper extAttributesHelper;

  public StoRMServlet(PathResolver resolver,
    ExtendedAttributesHelper attributesHelper) {

    pathResolver = resolver;
    extAttributesHelper = attributesHelper;
  }

  @Override
  public Resource getResource(String pathInContext) {

    try {

      String resolvedPath = pathResolver.resolvePath(pathInContext);

      if (resolvedPath == null) {
        return null;
      }

      File f = new File(resolvedPath);

      if (!f.exists()) {
        return null;
      }

      return Resource.newResource(f);
    } catch (IOException e) {
      logger.error("Error resolving resource {}: {}.", pathInContext,
        e.getMessage(), e);
      return null;
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    super.doGet(request, response);

    addChecksumHeader(request, response);

  }

  private void addChecksumHeader(HttpServletRequest request,
    HttpServletResponse response) {

    int status = response.getStatus();

    if (status == HttpServletResponse.SC_OK
      || status == HttpServletResponse.SC_PARTIAL_CONTENT) {

      logger.debug("Retrieving checksum value ...");

      try {

        File f = getResource(request.getPathInfo()).getFile();

        String cValue = extAttributesHelper.getChecksumAttribute(f);

        logger.debug("Checksum value for file {} is '{}'", f, cValue);

        if (!cValue.isEmpty()) {

          response.setHeader("Digest", "adler32=" + cValue);

        }

      } catch (IOException e) {

        logger.error("Unable to retrieve file checksum value: {}",
          e.getMessage());
        e.printStackTrace();

      }

    } else {

      logger
        .debug("Checksum value not computed cause response status is not successful");

    }
  }
}
