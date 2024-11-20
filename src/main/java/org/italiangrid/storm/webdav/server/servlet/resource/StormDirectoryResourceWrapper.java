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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.italiangrid.storm.webdav.authn.AuthenticationUtils;
import org.italiangrid.storm.webdav.config.OAuthProperties;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

// Jetty 12 does not permit to rewrite directory listing, but only changing the
// CSS Style, so this is a wrapper to lie to Jetty saying that the directory is
// a file and sending the HTML list view of the directory as file content.
public class StormDirectoryResourceWrapper extends Resource {

  public static final String JETTY_DIR_TEMPLATE = "jetty-dir";

  final Resource delegate;
  final TemplateEngine engine;
  final OAuthProperties oauthProperties;
  final ServiceConfigurationProperties serviceConfig;
  final String pathInContext;

  public StormDirectoryResourceWrapper(OAuthProperties oauth,
      ServiceConfigurationProperties serviceConfig, TemplateEngine engine, Resource delegate,
      String pathInContext) {

    this.oauthProperties = oauth;
    this.engine = engine;
    this.delegate = delegate;
    this.serviceConfig = serviceConfig;
    this.pathInContext = pathInContext;

  }

  // Adapted from
  // https://github.com/jetty/jetty.project/blob/jetty-12.0.x/jetty-core/jetty-server/src/main/java/org/eclipse/jetty/server/ResourceListing.java
  /**
   * <p>
   * Encode any characters that could break the URI string in an HREF.
   * </p>
   *
   * <p>
   * Such as: {@code <a href="/path/to;<script>Window.alert('XSS'+'%20'+'here');</script>">Link</a>}
   * </p>
   * <p>
   * The above example would parse incorrectly on various browsers as the "<" or '"' characters
   * would end the href attribute value string prematurely.
   * </p>
   *
   * @param raw the raw text to encode.
   * @return the defanged text.
   */
  private static String hrefEncodeURI(String raw) {
    StringBuilder buf = null;

    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c == '\'' || c == '"' || c == '<' || c == '>') {
        buf = new StringBuilder(raw.length() << 1);
        break;
      }
    }
    if (buf == null)
      return raw;

    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case '"' -> buf.append("%22");
        case '\'' -> buf.append("%27");
        case '<' -> buf.append("%3C");
        case '>' -> buf.append("%3E");
        default -> buf.append(c);
      }
    }

    return buf.toString();
  }

  public String getListHTML(String base) {
    base = URIUtil.canonicalPath(base);

    List<Resource> rawListing = list();

    Context context = new Context();

    String decodedBase = URIUtil.decodePath(base);

    String title = StringUtil.sanitizeXmlString(decodedBase);

    context.setVariable("title", title);
    context.setVariable("storm", serviceConfig.getHostnames().get(0));
    context.setVariable("authn", SecurityContextHolder.getContext().getAuthentication());
    context.setVariable("authnSubject", AuthenticationUtils
      .getPalatableSubject(SecurityContextHolder.getContext().getAuthentication()));

    context.setVariable("oidcEnabled", oauthProperties.isEnableOidc());

    String encodedBase = hrefEncodeURI(decodedBase);

    String parentDir = URIUtil.addPaths(encodedBase, "../");

    Collections.sort(rawListing, (a, b) -> a.getFileName().compareTo(b.getFileName()));

    List<StormFsResourceView> resources = new ArrayList<>();

    for (Resource r : rawListing) {
      resources.add(StormFsResourceView.builder()
        .withName(r.getFileName())
        .withPath(URIUtil.addEncodedPaths(encodedBase, r.getFileName()))
        .withIsDirectory(r.isDirectory())
        .withLastModificationTime(Date.from(r.lastModified()))
        .withSizeInBytes(r.length())
        .build());
    }


    context.setVariable("parentDir", parentDir);
    context.setVariable("resources", resources);
    return engine.process(JETTY_DIR_TEMPLATE, context);
  }

  @Override
  public boolean exists() {
    return delegate.exists();
  }

  @Override
  public String getFileName() {
    return delegate.getFileName();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Path getPath() {
    // We must lie so Jetty uses our overrided newInputStream instead of using
    // file path to read the content, because this is actually a directory
    return null;
  }

  @Override
  public URI getURI() {
    return delegate.getURI();
  }

  @Override
  public boolean isDirectory() {
    // We must lie otherwise Jetty uses its non-overridable directory listing
    return false;
  }

  @Override
  public boolean isReadable() {
    return delegate.isReadable();
  }

  @Override
  public Instant lastModified() {
    return delegate.lastModified();
  }

  @Override
  public long length() {
    return delegate.length();
  }

  @Override
  public List<Resource> list() {
    return delegate.list();
  }

  @Override
  public InputStream newInputStream() throws IOException {
    return new ByteArrayInputStream(getListHTML(pathInContext).getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Resource resolve(String subUriPath) {
    return delegate.resolve(subUriPath);
  }
}
