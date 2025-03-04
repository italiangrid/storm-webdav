// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.vomap;

import java.util.Set;

public interface VOMembershipSource {

  public String getVOName();

  public Set<String> getVOMembers();

}
