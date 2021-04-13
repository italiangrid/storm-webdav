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
package org.italiangrid.storm.webdav.server.servlet.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
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

public class StormResourceWrapper extends Resource {

  public static final String JETTY_DIR_TEMPLATE = "jetty-dir";

  final Resource delegate;
  final TemplateEngine engine;
  final OAuthProperties oauthProperties;
  final ServiceConfigurationProperties serviceConfig;

  public StormResourceWrapper(OAuthProperties oauth, ServiceConfigurationProperties serviceConfig,
      TemplateEngine engine, Resource delegate) {

    this.oauthProperties = oauth;
    this.engine = engine;
    this.delegate = delegate;
    this.serviceConfig = serviceConfig;

  }

  /**
   * Encode any characters that could break the URI string in an HREF. Such as <a
   * href="/path/to;<script>Window.alert("XSS"+'%20'+"here");</script>">Link</a>
   *
   * The above example would parse incorrectly on various browsers as the "<" or '"' characters
   * would end the href attribute value string prematurely.
   *
   * @param raw the raw text to encode.
   * @return the defanged text.
   */
  private static String hrefEncodeURI(String raw) {

    StringBuffer buf = null;

    loop: for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case '\'':
        case '"':
        case '<':
        case '>':
          buf = new StringBuffer(raw.length() << 1);
          break loop;
      }
    }
    if (buf == null)
      return raw;

    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case '"':
          buf.append("%22");
          continue;
        case '\'':
          buf.append("%27");
          continue;
        case '<':
          buf.append("%3C");
          continue;
        case '>':
          buf.append("%3E");
          continue;
        default:
          buf.append(c);
          continue;
      }
    }

    return buf.toString();
  }


  @Override
  public String getListHTML(String base, boolean parent, String query) throws IOException {

    base = URIUtil.canonicalPath(base);
    if (base == null || !isDirectory())
      return null;

    String[] rawListing = list();
    if (rawListing == null) {
      return null;
    }

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

    Arrays.sort(rawListing);

    List<StormFsResourceView> resources = new ArrayList<>();

    for (String l : rawListing) {
      Resource r = addPath(l);
      resources.add(StormFsResourceView.builder()
        .withName(l)
        .withPath(URIUtil.addEncodedPaths(encodedBase, URIUtil.encodePath(l)))
        .withIsDirectory(r.isDirectory())
        .withLastModificationTime(new Date(r.lastModified()))
        .withSizeInBytes(r.length())
        .build());
    }


    context.setVariable("parentDir", parentDir);
    context.setVariable("resources", resources);
    return engine.process(JETTY_DIR_TEMPLATE, context);
  }

  @Override
  public boolean isContainedIn(Resource r) throws MalformedURLException {

    return delegate.isContainedIn(r);
  }

  @Override
  public void close() {

    delegate.close();
  }

  @Override
  public boolean exists() {
    return delegate.exists();
  }

  @Override
  public boolean isDirectory() {
    return delegate.isDirectory();
  }

  @Override
  public long lastModified() {
    return delegate.lastModified();
  }

  @Override
  public long length() {
    return delegate.length();
  }

  @SuppressWarnings("deprecation")
  @Override
  public URL getURL() {
    return delegate.getURL();
  }

  @Override
  public File getFile() throws IOException {
    return delegate.getFile();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override
  public ReadableByteChannel getReadableByteChannel() throws IOException {
    return delegate.getReadableByteChannel();
  }

  @Override
  public boolean delete() throws SecurityException {
    return delegate.delete();
  }

  @Override
  public boolean renameTo(Resource dest) throws SecurityException {
    return delegate.renameTo(dest);
  }

  @Override
  public String[] list() {
    return delegate.list();
  }

  @Override
  public Resource addPath(String path) throws IOException, MalformedURLException {
    return delegate.addPath(path);
  }

  @Override
  public void writeTo(OutputStream out, long start, long count) throws IOException {

    try (InputStream in = getInputStream()) {
      in.skip(start);
      if (count < 0) {
        internalCopy(in, out);
      } else {
        internalCopy(in, out, count);
      }
    }

  }

  private void internalCopy(InputStream in, OutputStream out, long byteCount) throws IOException {

    int bufferSize = serviceConfig.getBuffer().getFileBufferSizeBytes();
    byte[] buffer = new byte[bufferSize];
    int len = bufferSize;

    if (byteCount >= 0) {
      while (byteCount > 0) {
        int max = byteCount < bufferSize ? (int) byteCount : bufferSize;
        len = in.read(buffer, 0, max);

        if (len == -1)
          break;

        byteCount -= len;
        out.write(buffer, 0, len);
      }
    } else {
      while (true) {
        len = in.read(buffer, 0, bufferSize);
        if (len < 0)
          break;
        out.write(buffer, 0, len);
      }
    }

  }

  private void internalCopy(InputStream in, OutputStream out) throws IOException {
    internalCopy(in, out, -1);
  }


}
