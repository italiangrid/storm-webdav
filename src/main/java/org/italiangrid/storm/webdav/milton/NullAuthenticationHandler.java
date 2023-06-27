/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.milton;

import java.util.List;

import io.milton.http.AuthenticationHandler;
import io.milton.http.Request;
import io.milton.resource.Resource;

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
  public void appendChallenges(Resource resource, Request request,
    List<String> challenges) {

  }

  @Override
  public boolean isCompatible(Resource resource, Request request) {

    return true;
  }

  @Override
  public boolean credentialsPresent(Request request) {

    return true;
  }

}
