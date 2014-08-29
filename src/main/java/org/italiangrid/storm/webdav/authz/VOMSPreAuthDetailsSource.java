package org.italiangrid.storm.webdav.authz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.voms.VOMSAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

public class VOMSPreAuthDetailsSource
	implements
	AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails> {
	
	private static final Logger logger = LoggerFactory.getLogger(VOMSPreAuthDetailsSource.class);
	
	private final VOMSAttributesExtractor attributesHelper;
	
	public VOMSPreAuthDetailsSource() {
		attributesHelper = new DefaultVOMSAttributesExtractor();
	}
	
	@Override
	public PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails buildDetails(
		HttpServletRequest request) {
		return new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request,
			getVOMSGrantedAuthorities(request));
	}
	
	private Collection<? extends GrantedAuthority> getVOMSGrantedAuthorities(HttpServletRequest request){
		
		List<VOMSAttribute> attributes = attributesHelper.getAttributes(request);
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		
		for (VOMSAttribute a: attributes){
			if (logger.isDebugEnabled()){
				logger.debug("Adding VO authority: {}", a.getVO());
			}
			authorities.add(new VOMSVOGrantedAuthority(a.getVO()));
		}
		
		return Collections.unmodifiableList(authorities);
	}
}
