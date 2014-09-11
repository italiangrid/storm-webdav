/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014.
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
package org.italiangrid.storm.webdav.server;

import org.italiangrid.storm.webdav.spring.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

public class Main {

  public static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    StatusPrinter.printInCaseOfErrorsOrWarnings((LoggerContext) LoggerFactory
      .getILoggerFactory());

    log.info("StoRM WebDAV server v. {}", Version.version());

    @SuppressWarnings("resource")
    ApplicationContext context = new AnnotationConfigApplicationContext(
      AppConfig.class);

    ServerLifecycle server = context.getBean(ServerLifecycle.class);
    server.start();

  }
}
