/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.util.URIUtil;

import io.milton.http.Auth;
import io.milton.servlet.ServletRequest;

public class StoRMMiltonRequest extends ServletRequest {

  private static final String regex = "(http.*:\\d*)/webdav/(.*)$";
  private static final Pattern p = Pattern.compile(regex);

  public StoRMMiltonRequest(HttpServletRequest r, ServletContext servletContext) {

    super(r, servletContext);
  }

  @Override
  public String getDestinationHeader() {

    String destHeaderValue = super.getDestinationHeader();
    if (destHeaderValue == null)
      return null;

    Matcher m = p.matcher(destHeaderValue);
    if (m.matches()) {
      return String.format("%s/%s", m.group(1), m.group(2));
    }
    return destHeaderValue;
  }

  @Override
  public String getAbsolutePath() {
    return URIUtil.compactPath(super.getAbsolutePath());
  }

  @Override
  public Auth getAuthorization() {
    // Always return null as milton is confused by the OAuth2 Bearer scheme
    return null;
  }
  
}
