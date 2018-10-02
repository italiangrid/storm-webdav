/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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

import com.google.common.collect.ImmutableSet;

public interface TransferConstants {

  String SOURCE_HEADER = "Source";
  String DESTINATION_HEADER = "Destination";
  String OVERWRITE_HEADER = "Overwrite";
  String REQUIRE_CHECKSUM_HEADER = "RequireChecksumVerification";
  String CREDENTIAL_HEADER = "Credential";
  
  String CREDENTIAL_HEADER_NONE_VALUE = "none";

  String TRANSFER_HEADER = "TransferHeader";
  String TRANSFER_HEADER_LC = TRANSFER_HEADER.toLowerCase();
  int TRANFER_HEADER_LENGTH = TRANSFER_HEADER.length();

  String HTTP = "http";
  String HTTPS = "https";

  String DAV = "dav";
  String DAVS = "davs";

  Set<String> SUPPORTED_PROTOCOLS =
      ImmutableSet.<String>builder().add(HTTP, HTTPS, DAV, DAVS).build();


}
