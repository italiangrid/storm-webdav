package org.italiangrid.storm.webdav.authz.vomsmap;

import java.util.Set;

import javax.security.auth.x500.X500Principal;


public interface VOMSMapDetailsService {
	
	public Set<String> getPrincipalVOs(X500Principal principal);
	
}
