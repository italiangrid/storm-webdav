package org.italiangrid.storm.webdav.fs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class DefaultFSStrategy implements FilesystemAccess {

	public static final Logger LOG = LoggerFactory.getLogger(DefaultFSStrategy.class);
	
	
	@Override
	public File mkdir(File parentDirectory, String dirName) {
		LOG.debug("mkdir: parent={}, dir={}", parentDirectory.getAbsolutePath(), dirName);
		
		File nd = new File(parentDirectory,dirName);
		
		if (!nd.mkdir()){
			LOG.warn("mkdir did not create {}", nd.getAbsolutePath());
		}
		return nd;
	}

	@Override
	public boolean rm(File f) {
		LOG.debug("rm: {}", f.getAbsolutePath());
		return f.delete();
	}

	@Override
	public void mv(File source, File dest) {
		LOG.debug("mv: source={}, dest={}", source.getAbsolutePath(), dest.getAbsolutePath());
		try {
			
			Files.move(source, dest);
			
		} catch (IOException e) {
			throw new StoRMWebDAVError(e.getMessage(),e);
		}
		
	}

	@Override
	public File[] ls(File dir, int limit) {
		LOG.debug("ls: dir={} limit={}", dir, limit);
		return null;
	}

	@Override
	public void cp(File source, File dest) {

		LOG.debug("cp: source={} target={}", 
			source.getAbsolutePath(),
			dest.getAbsolutePath());
		
		try {
			// TODO: default unix semantics implemented in guava copy,
			// if the file exists, its content is replaced.
			Files.copy(source, dest);
		} catch (IOException e) {
			
			throw new StoRMWebDAVError(e.getMessage(),e);
		}
	
	}

	@Override
	public File create(File file, InputStream in) {

		LOG.debug("create: file={}", file.getAbsolutePath());
		
		try {
			
			file.createNewFile();
			IOUtils.copy(in, new FileOutputStream(file));
			return file;
			
		} catch (IOException e) {
			throw new StoRMWebDAVError(e.getMessage(),e);
		}
		
	}

}
