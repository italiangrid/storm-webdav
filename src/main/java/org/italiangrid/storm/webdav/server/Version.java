package org.italiangrid.storm.webdav.server;

public class Version {

	private Version() {

	}

	public static String version() {

		String version = Version.class.getPackage().getImplementationVersion();
		if (version == null)
			return "N/A";
		return version;
	}

}
