// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;
import java.util.List;

public class NullAuthenticationHandler implements AuthenticationHandler {

  public static final String STORM_USER = "storm";

  @Override
  public boolean supports(Resource r, Request request) {

    return true;
  }

  @Override
  public Object authenticate(Resource resource, Request request) {

    return STORM_USER;
  }

  @Override
  public void appendChallenges(Resource resource, Request request, List<String> challenges) {}

  @Override
  public boolean isCompatible(Resource resource, Request request) {

    return true;
  }

  @Override
  public boolean credentialsPresent(Request request) {

    return true;
  }
}
