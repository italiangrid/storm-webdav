// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.macaroon;

import org.springframework.security.core.Authentication;

public interface MacaroonIssuerService {

  MacaroonResponseDTO createAccessToken(MacaroonRequestDTO request, Authentication auth);
  
}
