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
		StatusPrinter.printInCaseOfErrorsOrWarnings((LoggerContext) LoggerFactory.getILoggerFactory());

		log.info("StoRM WebDAV server v. {}", Version.version());
		
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
			AppConfig.class);
			
		ServerLifecycle server = context.getBean(ServerLifecycle.class);
		server.start();

	}
}
