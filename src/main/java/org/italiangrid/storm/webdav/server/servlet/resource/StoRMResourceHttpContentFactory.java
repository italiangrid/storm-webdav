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
package org.italiangrid.storm.webdav.server.servlet.resource;

import java.io.File;
import java.net.URI;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.content.HttpContent;
import org.eclipse.jetty.http.content.ResourceHttpContent;
import org.eclipse.jetty.http.content.ResourceHttpContentFactory;
import org.eclipse.jetty.util.resource.PathResourceFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;

public class StoRMResourceHttpContentFactory extends ResourceHttpContentFactory {
  public static final Logger LOG = LoggerFactory.getLogger(StoRMResourceHttpContentFactory.class);
  final PathResolver pathResolver;
  final TemplateEngine templateEngine;
  final ServiceConfigurationProperties serviceConfig;
  final OAuthProperties oauthProperties;
  MimeTypes mimeTypes;

  public StoRMResourceHttpContentFactory(Resource baseResource, MimeTypes mimeTypes,
      OAuthProperties oauthP, ServiceConfigurationProperties serviceConfig, PathResolver resolver,
      TemplateEngine engine) {
    super(baseResource, mimeTypes);
    this.mimeTypes = mimeTypes;
    pathResolver = resolver;
    oauthProperties = oauthP;
    templateEngine = engine;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public HttpContent getContent(String pathInContext) {

    String resolvedPath = pathResolver.resolvePath(pathInContext);

    if (resolvedPath == null) {
      return null;
    }

    File f = new File(resolvedPath);

    if (!f.exists()) {
      LOG.warn("File {} do not exist", resolvedPath);
      return null;
    }

    PathResourceFactory pathResourceFactory = new PathResourceFactory();
    try {
      if (f.isDirectory()) {
        StormDirectoryResourceWrapper resource =
            new StormDirectoryResourceWrapper(oauthProperties, serviceConfig, templateEngine,
                pathResourceFactory.newResource(new URI("file:" + resolvedPath)), pathInContext);
        return new ResourceHttpContent(resource, "text/html");
      } else {
        return new ResourceHttpContent(
            pathResourceFactory.newResource(new URI("file:" + resolvedPath)),
            mimeTypes.getMimeByExtension(pathInContext));
      }
    } catch (Exception e) {
      LOG.warn("PathResourceFactory exception: {}", e.getMessage());
    }

    return null;
  }
}
