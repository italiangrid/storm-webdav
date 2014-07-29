package org.italiangrid.storm.webdav.fs;


public class FilesystemAccessFactory {

	public static FilesystemAccess newFilesystemAccess(){
		return new DefaultFSStrategy();
	}
}
