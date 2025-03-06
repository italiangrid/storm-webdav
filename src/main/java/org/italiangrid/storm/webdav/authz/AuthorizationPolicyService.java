// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public interface AuthorizationPolicyService {

  Set<GrantedAuthority> getSAPermissions(Collection<? extends GrantedAuthority> authorities);

  Set<GrantedAuthority> getSAPermissions(Authentication authn);
}
