package org.italiangrid.storm.webdav.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.AuthenticationHandler;
import io.milton.http.http11.DefaultHttp11ResponseHandler.BUFFERING;

import com.google.common.collect.ImmutableList;


public class StoRMHTTPManagerBuilder extends HttpManagerBuilder{
	
	
	public StoRMHTTPManagerBuilder() {

		setDefaultStandardFilter(new StoRMMiltonBehaviour());
		setEnabledJson(false);
		
		setBuffering(BUFFERING.never);
		setEnableCompression(false);
		
		ImmutableList<AuthenticationHandler> authHandlers =
			ImmutableList.of((AuthenticationHandler)new NullAuthenticationHandler());
		
		setAuthenticationHandlers(authHandlers);
		
		setEnableBasicAuth(false);
    setEnableExpectContinue(false);
    setEnableFormAuth(false);
    setEnableCookieAuth(false);
    setEnableDigestAuth(false);
    
    
	}

}
