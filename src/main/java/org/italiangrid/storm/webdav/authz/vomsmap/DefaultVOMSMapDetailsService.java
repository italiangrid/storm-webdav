package org.italiangrid.storm.webdav.authz.vomsmap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.springframework.util.Assert;


public class DefaultVOMSMapDetailsService implements VOMSMapDetailsService {
	
	Set<VOMembershipProvider> providers;
		
	public DefaultVOMSMapDetailsService(Set<VOMembershipProvider> providers) {
		Assert.notNull(providers, "Please provide a non-null (but possibly empty) set of providers");
		this.providers = providers;
	}
	
	private String getSubjectFromPrincipal(X500Principal principal){
		Assert.notNull(principal);
		return principal.getName();
	}
	
	
	@Override
	public Set<String> getPrincipalVOs(X500Principal principal) {
		Assert.notNull(principal, "Principal cannot be null");
		
		HashSet<String> voNames = new HashSet<String>();
		
		for (VOMembershipProvider p: providers){
			
			if (p.hasSubjectAsMember(getSubjectFromPrincipal(principal))){
				voNames.add(p.getVOName());
			}
		}
		
		return Collections.unmodifiableSet(voNames);
	}

}
