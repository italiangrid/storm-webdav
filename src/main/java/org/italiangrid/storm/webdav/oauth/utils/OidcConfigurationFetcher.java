// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.utils;

import java.net.URI;
import java.util.Map;

import com.nimbusds.jose.KeySourceException;

public interface OidcConfigurationFetcher {

  Map<String, Object> loadConfigurationForIssuer(String issuer);

  String loadJWKSourceForURL(URI uri) throws KeySourceException;

}
