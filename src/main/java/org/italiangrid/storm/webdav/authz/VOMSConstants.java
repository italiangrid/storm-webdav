// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import java.util.regex.Pattern;

public final class VOMSConstants {

  public static final String VOMS_USER_HEADER = "X-VOMS-voms_user";
  public static final String SSL_CLIENT_EE_S_DN_HEADER = "X-VOMS-ssl_client_ee_s_dn";
  public static final String VOMS_USER_CA_HEADER = "X-VOMS-voms_user_ca";
  public static final String SSL_CLIENT_EE_I_DN_HEADER = "X-VOMS-ssl_client_ee_i_dn";
  public static final String VOMS_FQANS_HEADER = "X-VOMS-voms_fqans";
  public static final String VOMS_VO_HEADER = "X-VOMS-voms_vo";
  public static final String VOMS_SERVER_URI_HEADER = "X-VOMS-voms_server_uri";
  public static final String VOMS_NOT_BEFORE_HEADER = "X-VOMS-voms_not_before";
  public static final String VOMS_NOT_AFTER_HEADER = "X-VOMS-voms_not_after";
  public static final String VOMS_GENERIC_ATTRIBUTES_HEADER = "X-VOMS-voms_generic_attributes";
  public static final String VOMS_SERIAL_HEADER = "X-VOMS-voms_serial";

  public static final String VOMS_DATE_FORMAT = "yyyyMMddHHmmss'Z'";

  public static final String VOMS_GENERIC_ATTRIBUTES_REGEX = "n=(\\S*) v=(\\S*) q=(\\S*)";
  public static final Pattern VOMS_GENERIC_ATTRIBUTES_PATTERN =
      Pattern.compile(VOMS_GENERIC_ATTRIBUTES_REGEX);

  private VOMSConstants() {}
}
