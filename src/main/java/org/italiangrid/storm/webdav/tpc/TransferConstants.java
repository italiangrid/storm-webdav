// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import java.util.Set;
import java.util.regex.Pattern;

public final class TransferConstants {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String CLIENT_INFO_HEADER = "ClientInfo";
  public static final String SOURCE_HEADER = "Source";
  public static final String DESTINATION_HEADER = "Destination";
  public static final String OVERWRITE_HEADER = "Overwrite";
  public static final String REQUIRE_CHECKSUM_HEADER = "RequireChecksumVerification";
  public static final String CREDENTIAL_HEADER = "Credential";

  public static final String CREDENTIAL_HEADER_NONE_VALUE = "none";

  public static final String TRANSFER_HEADER = "TransferHeader";
  public static final String TRANSFER_HEADER_LC = TRANSFER_HEADER.toLowerCase();
  public static final int TRANFER_HEADER_LENGTH = TRANSFER_HEADER.length();

  public static final String HTTP = "http";
  public static final String HTTPS = "https";

  public static final String DAV = "dav";
  public static final String DAVS = "davs";

  public static final Set<String> SUPPORTED_PROTOCOLS = Set.of(HTTP, HTTPS, DAV, DAVS);

  public static final String WEBDAV_PATH_REGEX = "/webdav/(.*)$";
  public static final Pattern WEBDAV_PATH_PATTERN = Pattern.compile(WEBDAV_PATH_REGEX);

  private TransferConstants() {}
}
