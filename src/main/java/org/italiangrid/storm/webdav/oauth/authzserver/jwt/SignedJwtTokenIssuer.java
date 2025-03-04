// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.jwt;

import org.italiangrid.storm.webdav.oauth.authzserver.AccessTokenRequest;
import org.italiangrid.storm.webdav.oauth.authzserver.ResourceAccessTokenRequest;
import org.springframework.security.core.Authentication;

import com.nimbusds.jwt.SignedJWT;

public interface SignedJwtTokenIssuer {
  
  public SignedJWT createAccessToken(AccessTokenRequest request, Authentication authentication);

  public SignedJWT createResourceAccessToken(ResourceAccessTokenRequest request,
      Authentication authentication);

}
