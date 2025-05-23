// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import static io.milton.property.PropertySource.PropertyAccessibility.READ_ONLY;

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.http.http11.PartialllyUpdateableResource;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.property.PropertySource.PropertySetException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MultiNamespaceCustomPropertyResource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.DiskQuotaExceeded;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.utils.RangeCopyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoRMFileResource extends StoRMResource
    implements DeletableResource,
        CopyableResource,
        MultiNamespaceCustomPropertyResource,
        GetableResource,
        PartialllyUpdateableResource {

  private static final FileNameMap MIME_TYPE_MAP = URLConnection.getFileNameMap();

  public static final String STORM_NAMESPACE_URI = "http://storm.italiangrid.org/2014/webdav";
  public static final String PROPERTY_CHECKSUM = "Checksum";

  public static final String DISK_QUOTA_EXCEEDED = "Disk quota exceeded";

  private static final Map<QName, PropertyMetaData> PROPERTY_METADATA =
      Map.of(
          new QName(STORM_NAMESPACE_URI, PROPERTY_CHECKSUM),
          new PropertyMetaData(READ_ONLY, String.class));

  private static final Logger logger = LoggerFactory.getLogger(StoRMFileResource.class);

  public StoRMFileResource(StoRMResourceFactory factory, File f) {
    super(factory, f);
  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {

    try {
      getFilesystemAccess().rm(getFile());
    } catch (NoSuchFileException e) {
      logger.warn("Unable to remove file {}: {}", getFile(), e.getMessage());
    } catch (IOException e) {
      logger.error("Unable to remove file {}: {}", getFile(), e.getMessage());
      throw new StoRMWebDAVError(e);
    }
  }

  protected void handleIOException(IOException e) {

    if (DISK_QUOTA_EXCEEDED.equals(e.getMessage())) {
      throw new DiskQuotaExceeded(e.getMessage(), e);
    }

    throw new StoRMWebDAVError(e.getMessage(), e);
  }

  @Override
  public void copyTo(CollectionResource toCollection, String name)
      throws NotAuthorizedException, BadRequestException, ConflictException {

    StoRMDirectoryResource dir = (StoRMDirectoryResource) toCollection;
    File destFile = dir.childrenFile(name);
    getFilesystemAccess().cp(getFile(), destFile);
  }

  @Override
  public void replaceContent(InputStream in, Long length)
      throws BadRequestException, ConflictException, NotAuthorizedException {

    logger.warn("Replacing file content: {}", getFile().getAbsolutePath());

    try {

      getResourceFactory().getReplaceContentStrategy().replaceContent(in, length, getFile());

    } catch (FileNotFoundException e) {
      throw new ResourceNotFound(e);
    } catch (IOException e) {
      handleIOException(e);
    }
  }

  protected void validateRange(Range range) {
    long fileSize = getFile().length();

    if (range.getStart() == null) {
      throw new StoRMWebDAVError("Invalid range: range start not defined");
    }

    if (range.getStart() >= fileSize) {
      throw new StoRMWebDAVError("Invalid range: range start out of bounds");
    }

    if (range.getFinish() != null && range.getFinish() > fileSize) {
      throw new StoRMWebDAVError("Invalid range: range end out of bounds");
    }
  }

  @Override
  public void replacePartialContent(Range range, InputStream in) {

    logger.warn("Replacing partial file content: {}", getFile().getAbsolutePath());

    validateRange(range);

    long rangeStart = range.getStart();
    long rangeEnd = getFile().length();

    if (range.getFinish() != null && range.getFinish() < rangeEnd) {
      rangeEnd = range.getFinish();
    }

    long rangeLength = rangeEnd - rangeStart;

    try {
      RangeCopyHelper.rangeCopy(in, getFile(), rangeStart, rangeLength);
    } catch (IOException e) {

      handleIOException(e);
    }

    // Need to update the checksum...
    calculateChecksum();
  }

  protected void calculateChecksum() {
    try (Adler32ChecksumInputStream cis =
        new Adler32ChecksumInputStream(new BufferedInputStream(new FileInputStream(getFile())))) {

      byte[] buffer = new byte[8192];

      while (cis.read(buffer) != -1) {
        // do nothing, just read
      }

      getExtendedAttributesHelper().setChecksumAttribute(getFile(), cis.getChecksumValue());

    } catch (IOException e) {
      throw new StoRMWebDAVError(e);
    }
  }

  @Override
  public Object getProperty(QName name) {

    if (name.getNamespaceURI().equals(STORM_NAMESPACE_URI)
        && name.getLocalPart().equals(PROPERTY_CHECKSUM)) {
      try {
        return getExtendedAttributesHelper().getChecksumAttribute(getFile());
      } catch (IOException e) {
        logger.warn("Errror getting checksum value for file: {}", getFile().getAbsolutePath(), e);
        return null;
      }
    }

    throw new StoRMWebDAVError("Unknown property: " + name);
  }

  @Override
  public void setProperty(QName name, Object value)
      throws PropertySetException, NotAuthorizedException {

    throw new UnsupportedOperationException(
        "StoRM WebDAV does not support setting DAV properties.");
  }

  @Override
  public PropertyMetaData getPropertyMetaData(QName name) {

    return PROPERTY_METADATA.get(name);
  }

  @Override
  public List<QName> getAllPropertyNames() {

    return new ArrayList<>(PROPERTY_METADATA.keySet());
  }

  @Override
  public void sendContent(
      OutputStream out, Range range, Map<String, String> params, String contentType)
      throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {

    // Not implemented
    throw new UnsupportedOperationException();
  }

  @Override
  public Long getMaxAgeSeconds(Auth auth) {

    return null;
  }

  @Override
  public String getContentType(String accepts) {

    return MIME_TYPE_MAP.getContentTypeFor(getFile().getAbsolutePath());
  }

  @Override
  public Long getContentLength() {

    return getFile().length();
  }

  @Override
  public String toString() {

    return "StoRMFileResource [resourceFactory=" + resourceFactory + ", file=" + file + "]";
  }
}
