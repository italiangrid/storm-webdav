// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc;

import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.net.NamedEndpoint;

public class TpcSchemePortResolver extends DefaultSchemePortResolver {

  @Override
  public int resolve(String scheme, final NamedEndpoint endpoint) {
    if (scheme.equals(TransferConstants.DAV)) {
      scheme = URIScheme.HTTP.toString();
    } else if (scheme.equals(TransferConstants.DAVS)) {
      scheme = URIScheme.HTTPS.toString();
    }
    return super.resolve(scheme, endpoint);
  }

}
