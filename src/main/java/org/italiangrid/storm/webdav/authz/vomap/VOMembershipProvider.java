package org.italiangrid.storm.webdav.authz.vomap;

public interface VOMembershipProvider {

  public String getVOName();

  public boolean hasSubjectAsMember(String subject);

}
