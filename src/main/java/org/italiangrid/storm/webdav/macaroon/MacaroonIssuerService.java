package org.italiangrid.storm.webdav.macaroon;

import org.springframework.security.core.Authentication;

public interface MacaroonIssuerService {

  MacaroonResponseDTO createAccessToken(MacaroonRequestDTO request, Authentication auth);
  
}
