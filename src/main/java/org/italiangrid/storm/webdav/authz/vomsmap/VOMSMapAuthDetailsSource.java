package org.italiangrid.storm.webdav.authz.vomsmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.AbstractVOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.voms.VOMSAttribute;
import org.springframework.security.core.GrantedAuthority;

public class VOMSMapAuthDetailsSource extends AbstractVOMSAuthDetailsSource {

	private final VOMSMapDetailsService mapService;

	public VOMSMapAuthDetailsSource(VOMSMapDetailsService mds) {

		mapService = mds;
	}

	
	protected List<VOMSAttribute> voNamesToAttributes(Set<String> voNames) {

		if (voNames == null || voNames.isEmpty())
			return Collections.emptyList();
		
		List<VOMSAttribute> attrs = new ArrayList<VOMSAttribute>();
		for (String name: voNames){
			attrs.add(new MappedVOMSAttribute(name));
		}
		
		return attrs;
	}

	@Override
	public Collection<GrantedAuthority> getVOMSGrantedAuthorities(
		HttpServletRequest request) {

		X500Principal principal = getPrincipalFromRequest(request);
		
		if (principal == null){
			return Collections.emptyList();
		}
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		for (String voName: mapService.getPrincipalVOs(principal)){
			if (logger.isDebugEnabled()) {
				logger.debug("Adding VO authority: {}", voName);
			}
			authorities.add(new VOMSVOAuthority(voName));
		}
		
		return authorities;
	}

}
