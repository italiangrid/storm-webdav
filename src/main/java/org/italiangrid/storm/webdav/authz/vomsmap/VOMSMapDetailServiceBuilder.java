package org.italiangrid.storm.webdav.authz.vomsmap;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class VOMSMapDetailServiceBuilder {

	private static final Logger logger = LoggerFactory.getLogger(VOMSMapDetailServiceBuilder.class);
	
	private final ServiceConfiguration serviceConf;
	
	@Autowired
	public VOMSMapDetailServiceBuilder(ServiceConfiguration conf) {
		this.serviceConf = conf;
	}
	
	private void directorySanityChecks(File directory) {

		if (!directory.exists())
			throw new IllegalArgumentException(
				"VOMS map files configuration directory does not exists: "
					+ directory.getAbsolutePath());

		if (!directory.isDirectory())
			throw new IllegalArgumentException(
				"VOMS map files configuration directory is not a directory: "
					+ directory.getAbsolutePath());

		if (!directory.canRead())
			throw new IllegalArgumentException(
				"VOMS map files configuration directory is not readable: "
					+ directory.getAbsolutePath());

		if (!directory.canExecute())
			throw new IllegalArgumentException(
				"VOMS map files configuration directory is not traversable: "
					+ directory.getAbsolutePath());

	}
	
	public VOMSMapDetailsService build(){
		
		if (!serviceConf.enableVOMSMapFiles()){
			logger.info("VOMS Map files disabled.");
			return null;
		}
		
		File configDir = new File(serviceConf.getVOMSMapFilesConfigDir());
		directorySanityChecks(configDir);
		
		File[] files = configDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});
		
		if (files.length == 0){
			logger.warn("No mapfiles found in {}.");
			return null;
		}
		
		Set<VOMembershipProvider> providers = new HashSet<VOMembershipProvider>();
		for (File f: files){
			try{
				
				VOMembershipProvider prov = new DefaultVOMembershipProvider(f.getName(), 
					new MapfileVOMembershipSource(f.getName(), f));
				
				providers.add(prov);
				
			}catch(Throwable t){
				logger.error("Error parsing mapfile {}: {}", f.getAbsolutePath(), t.getMessage(),t);
				continue;
			}
		}
		
		return new DefaultVOMSMapDetailsService(providers);
	}
	
}
