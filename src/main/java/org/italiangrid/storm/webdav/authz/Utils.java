package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;


public class Utils {
	
	private Utils() {}

	public static X509Certificate[] getCertificateChainFromRequest(HttpServletRequest request){
		
		return (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate"); 
	}
}
