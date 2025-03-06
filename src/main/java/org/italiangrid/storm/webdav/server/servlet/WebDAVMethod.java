// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.server.servlet;

public enum WebDAVMethod {
  OPTIONS,
  PUT,
  DELETE,
  PROPFIND,
  PROPPATCH,
  MKCOL,
  MOVE,
  COPY,
  LOCK,
  UNLOCK
}
