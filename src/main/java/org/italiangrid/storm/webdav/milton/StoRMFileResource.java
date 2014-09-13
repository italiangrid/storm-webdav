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
package org.italiangrid.storm.webdav.milton;

import static io.milton.property.PropertySource.PropertyAccessibility.READ_ONLY;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.property.PropertySource.PropertyMetaData;
import io.milton.property.PropertySource.PropertySetException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MultiNamespaceCustomPropertyResource;
import io.milton.resource.ReplaceableResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.italiangrid.storm.webdav.checksum.Adler32ChecksumInputStream;
import org.italiangrid.storm.webdav.error.ResourceNotFound;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class StoRMFileResource extends StoRMResource implements
  DeletableResource, CopyableResource, ReplaceableResource,
  MultiNamespaceCustomPropertyResource, GetableResource {

  private static final FileNameMap MIME_TYPE_MAP = URLConnection
    .getFileNameMap();

  public static final String STORM_NAMESPACE_URI = "http://storm.italiangrid.org/2014/webdav";
  public static final String PROPERTY_CHECKSUM = "Checksum";

  private static final ImmutableMap<QName, PropertyMetaData> PROPERTY_METADATA = new ImmutableMap.Builder<QName, PropertyMetaData>()
    .put(new QName(STORM_NAMESPACE_URI, PROPERTY_CHECKSUM),
      new PropertyMetaData(READ_ONLY, String.class)).build();

  private static final Logger logger = LoggerFactory
    .getLogger(StoRMFileResource.class);

  public StoRMFileResource(StoRMResourceFactory factory, File f) {

    super(factory, f);

  }

  @Override
  public void delete() throws NotAuthorizedException, ConflictException,
    BadRequestException {

    getFilesystemAccess().rm(getFile());

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

    try {
      Adler32ChecksumInputStream cis = new Adler32ChecksumInputStream(in);
      OutputStream os = new FileOutputStream(getFile());

      IOUtils.copy(cis, os);
      IOUtils.closeQuietly(os);

      getExtendedAttributesHelper().setChecksumAttribute(getFile(),
        cis.getChecksumValue());

    } catch (FileNotFoundException e) {
      throw new ResourceNotFound(e);
    } catch (IOException e) {
      throw new StoRMWebDAVError(e);
    }
  }

  @Override
  public Object getProperty(QName name) {

    if (name.getNamespaceURI().equals(STORM_NAMESPACE_URI)) {
      if (name.getLocalPart().equals(PROPERTY_CHECKSUM)) {
        try {
          return getExtendedAttributesHelper().getChecksumAttribute(getFile());
        } catch (IOException e) {
          logger.warn("Errror getting checksum value for file: {}", getFile()
            .getAbsolutePath(), e);
          return null;
        }
      }
    }

    throw new StoRMWebDAVError("Unknown property: " + name);
  }

  @Override
  public void setProperty(QName name, Object value)
    throws PropertySetException, NotAuthorizedException {

    throw new NotImplementedException(
      "StoRM WebDAV does not support setting DAV properties.");
  }

  @Override
  public PropertyMetaData getPropertyMetaData(QName name) {

    return PROPERTY_METADATA.get(name);
  }

  @Override
  public List<QName> getAllPropertyNames() {

    return PROPERTY_METADATA.keySet().asList();
  }

  @Override
  public void sendContent(OutputStream out, Range range,
    Map<String, String> params, String contentType) throws IOException,
    NotAuthorizedException, BadRequestException, NotFoundException {

    throw new NotImplementedException();

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

    return "StoRMFileResource [resourceFactory=" + resourceFactory + ", file="
      + file + "]";
  }

}
