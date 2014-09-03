package org.italiangrid.storm.webdav.authz.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

public class CustomMethodAntPathRequestMatcher implements RequestMatcher {

	private static final Logger logger = org.slf4j.LoggerFactory
		.getLogger(CustomMethodAntPathRequestMatcher.class);
	
	private final String httpOrDAVMethod;
	private final AntPathRequestMatcher antMatcher;

	public CustomMethodAntPathRequestMatcher(String method, String pattern, boolean caseSensitive) {
		Assert.hasText(method, "Please provide a non-empty method");
		Assert.hasText(pattern, "Please provide a non-empty pattern.");
			
		this.httpOrDAVMethod = method;
		this.antMatcher = new AntPathRequestMatcher(pattern,null,caseSensitive);
	}
	
	@Override
	public boolean matches(HttpServletRequest request) {
		if (request.getMethod() != null && !request.getMethod().equals(httpOrDAVMethod)){
			if (logger.isDebugEnabled()){
				 logger.debug("Request '" + request.getMethod() + " " + getRequestPath(request) + "'"
           + " doesn't match '" + httpOrDAVMethod  + " " + antMatcher.getPattern());
			}
			return false;
		}
		
		// Method matches, delegate down request match
		return antMatcher.matches(request);
		
	}

	
	private String getRequestPath(HttpServletRequest request) {
    String url = request.getServletPath();

    if (request.getPathInfo() != null) {
        url += request.getPathInfo();
    }

    return url;
	}
}
