package org.italiangrid.storm.webdav.authz.vomsmap;

import java.util.Set;


public interface VOMembershipSource {
	
	public String getVOName();
	public Set<String> getVOMembers();

}
