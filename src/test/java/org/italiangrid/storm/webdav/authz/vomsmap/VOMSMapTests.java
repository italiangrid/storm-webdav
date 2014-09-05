package org.italiangrid.storm.webdav.authz.vomsmap;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;


public class VOMSMapTests {

	public static final String mySubject = "CN=Andrea Ceccanti,L=CNAF,OU=Personal Certificate,O=INFN,C=IT";
	@Test
	public void VOMapParserTest() {

		MapfileVOMembershipSource m = new MapfileVOMembershipSource("testers", 
			new File("src/test/resources/vomsmap/testers.map"));
		
		Assert.assertEquals("testers",m.getVOName());
		Assert.assertTrue(m.getVOMembers().contains(mySubject));
		
		Assert.assertFalse(m.getVOMembers().contains("CN=I am not Real, L=CNAF"));
		
	}

}
