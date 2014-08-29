package org.italiangrid.storm.webdav.server;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum JettyServerListener implements LifeCycle.Listener{

	INSTANCE;
	
	public static final Logger log = LoggerFactory.getLogger(JettyServerListener.class);
	
	@Override
	public void lifeCycleStarting(LifeCycle event) {

		log.info("StoRM WebDAV server starting...");
		
	}

	@Override
	public void lifeCycleStarted(LifeCycle event) {

		log.info("StoRM WebDAV server started.");
		
	}

	@Override
	public void lifeCycleFailure(LifeCycle event, Throwable cause) {

		log.error("StoRM WebDAV server failure: {}", cause.getMessage(), cause);
		
	}

	@Override
	public void lifeCycleStopping(LifeCycle event) {

		log.info("StoRM WebDAV server stopping.");
		
	}

	@Override
	public void lifeCycleStopped(LifeCycle event) {

		log.info("StoRM WebDAV server stopped.");
		
	}

}
