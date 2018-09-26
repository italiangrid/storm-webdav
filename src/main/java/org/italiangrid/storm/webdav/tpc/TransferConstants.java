package org.italiangrid.storm.webdav.tpc;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface TransferConstants {

  String SOURCE_HEADER = "Source";
  String DESTINATION_HEADER = "Destination";
  String OVERWRITE_HEADER = "Overwrite";
  String REQUIRE_CHECKSUM_HEADER = "RequireChecksumVerification";

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
