// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.vomap;

import java.util.Set;

import javax.security.auth.x500.X500Principal;

public interface VOMapDetailsService {

  public Set<String> getPrincipalVOs(X500Principal principal);

}
