package org.italiangrid.storm.webdav.spring.web;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;

@Component
public class MyLoaderListener extends ContextLoaderListener {

  private ApplicationContext parentContext;

  public MyLoaderListener(ApplicationContext ctxt) {

    parentContext = ctxt;
  }

  @Override
  protected ApplicationContext loadParentContext(ServletContext servletContext) {

    return parentContext;
  }

}
