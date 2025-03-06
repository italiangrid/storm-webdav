// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.spring.web;

import jakarta.servlet.ServletContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

public class AppContextLoaderListener extends ContextLoaderListener {

  private ApplicationContext parentContext;

  public AppContextLoaderListener(ApplicationContext ctxt) {

    parentContext = ctxt;
  }

  @Override
  protected ApplicationContext loadParentContext(ServletContext servletContext) {

    return parentContext;
  }
}
