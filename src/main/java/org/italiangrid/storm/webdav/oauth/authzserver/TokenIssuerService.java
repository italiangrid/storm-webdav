// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import org.springframework.security.core.Authentication;

public interface TokenIssuerService {

  TokenResponseDTO createAccessToken(
      AccessTokenRequest tokenRequest, Authentication authentication);
}
