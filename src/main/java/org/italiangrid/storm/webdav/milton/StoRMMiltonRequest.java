// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton;

import io.milton.http.Auth;
import io.milton.servlet.ServletRequest;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.util.URIUtil;
import org.italiangrid.storm.webdav.tpc.SwappedServletRequest;

public class StoRMMiltonRequest extends ServletRequest {

  private static final String REGEX = "(http.*:\\d*)/webdav/(.*)$";
  private static final Pattern PATTERN = Pattern.compile(REGEX);
  private boolean sendSuccessPerfMarker = false;

  public StoRMMiltonRequest(HttpServletRequest r, ServletContext servletContext) {

    super(r, servletContext);
    if (r instanceof SwappedServletRequest) {
      this.sendSuccessPerfMarker = true;
    }
  }

  @Override
  public String getDestinationHeader() {

    String destHeaderValue = super.getDestinationHeader();
    if (destHeaderValue == null) {
      return null;
    }

    Matcher m = PATTERN.matcher(destHeaderValue);
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

  public boolean sendSuccessPerfMarker() {
    return sendSuccessPerfMarker;
  }
}
