package org.italiangrid.storm.webdav.authz;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.voms.VOMSAttribute;


public interface VOMSAttributesExtractor {

	public List<VOMSAttribute> getAttributes(HttpServletRequest request);
	
}
