// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
