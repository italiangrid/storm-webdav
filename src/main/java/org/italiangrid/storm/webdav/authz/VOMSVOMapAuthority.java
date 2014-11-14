package org.italiangrid.storm.webdav.authz;

import org.springframework.security.core.GrantedAuthority;

public class VOMSVOMapAuthority implements GrantedAuthority,
  Comparable<VOMSVOMapAuthority> {

  /**
   * 
   */
  private static final long serialVersionUID = -455904635960596363L;

  private final String voName;

  private final String authority;

  public VOMSVOMapAuthority(String vo) {

    authority = String.format("VO_MAP(%s)", vo);
    voName = vo;
  }

  @Override
  public String getAuthority() {

    return authority;
  }

  public String getVoName() {

    return voName;
  }

  @Override
  public int compareTo(VOMSVOMapAuthority o) {

    return authority.compareTo(o.authority);
  }

}
