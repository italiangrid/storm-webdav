package org.italiangrid.storm.webdav.authz;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;


public class VOMSAuthenticationProvider extends
	PreAuthenticatedAuthenticationProvider implements AuthenticationManager{

	public VOMSAuthenticationProvider() {
		setThrowExceptionWhenTokenRejected(true);
		setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
	}
	
}
