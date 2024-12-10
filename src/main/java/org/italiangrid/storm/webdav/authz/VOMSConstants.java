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
package org.italiangrid.storm.webdav.authz;

public interface VOMSConstants {

  String VOMS_USER_HEADER = "X-VOMS-voms_user";
  String SSL_CLIENT_EE_S_DN_HEADER = "X-VOMS-ssl_client_ee_s_dn";
  String VOMS_USER_CA_HEADER = "X-VOMS-voms_user_ca";
  String SSL_CLIENT_EE_I_DN_HEADER = "X-VOMS-ssl_client_ee_i_dn";
  String VOMS_FQANS_HEADER = "X-VOMS-voms_fqans";
  String VOMS_VO_HEADER = "X-VOMS-voms_vo";
  String VOMS_SERVER_URI_HEADER = "X-VOMS-voms_server_uri";
  String VOMS_NOT_BEFORE_HEADER = "X-VOMS-voms_not_before";
  String VOMS_NOT_AFTER_HEADER = "X-VOMS-voms_not_after";
  String VOMS_GENERIC_ATTRIBUTES_HEADER = "X-VOMS-voms_generic_attributes";
  String VOMS_SERIAL_HEADER = "X-VOMS-voms_serial";

  String VOMS_DATE_FORMAT = "yyyyMMddHHmmss'Z'";

  String VOMS_GENERIC_ATTRIBUTES_REGEX = "n=(\\S*) v=(\\S*) q=(\\S*)";

}
