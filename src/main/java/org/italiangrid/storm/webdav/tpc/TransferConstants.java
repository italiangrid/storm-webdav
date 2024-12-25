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
