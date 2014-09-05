package org.italiangrid.storm.webdav.authz.vomsmap;



public interface VOMembershipProvider {
	
	public String getVOName();
	
	public boolean hasSubjectAsMember(String subject);
	
	public void refresh();
	
}
