package org.italiangrid.storm.webdav.milton;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;

import java.io.File;

import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoRMResourceFactory implements ResourceFactory {

  public static final Logger LOG = LoggerFactory
    .getLogger(StoRMResourceFactory.class);

  private final FilesystemAccess fs;

  private final ExtendedAttributesHelper attrsHelper;

  private final File rootPath;
  private final String contextPath;

  public StoRMResourceFactory(FilesystemAccess fs,
    ExtendedAttributesHelper attrsHelper, String root, String contextPath) {

    this.fs = fs;
    this.rootPath = new File(root);
    this.contextPath = contextPath;
    this.attrsHelper = attrsHelper;
  }

  private String stripContextPath(String url) {

    if (this.contextPath != null && contextPath.length() > 0) {
      url = url.replaceFirst(contextPath, "");
      LOG.debug("stripped context: " + url);
      return url;
    } else {
      return url;
    }
  }

  public File resolvePath(File root, String url) {

    Path path = Path.path(url);
    File f = root;
    for (String s : path.getParts()) {
      f = new File(f, s);
    }
    return f;
  }

  @Override
  public Resource getResource(String host, String path)
    throws NotAuthorizedException, BadRequestException {

    String strippedPath = stripContextPath(path);

    File requestedFile = resolvePath(rootPath, strippedPath);

    LOG.debug("getResource: path={}, resolvedPath={}", path,
      requestedFile.getAbsolutePath());

    if (!requestedFile.exists()) {
      LOG
        .warn(
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
}
