// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.vomap;

public interface VOMembershipProvider {

  String getVOName();

  boolean hasSubjectAsMember(String subject);
}
