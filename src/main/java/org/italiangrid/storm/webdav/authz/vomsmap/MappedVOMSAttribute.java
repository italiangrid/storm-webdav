package org.italiangrid.storm.webdav.authz.vomsmap;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSGenericAttribute;

public class MappedVOMSAttribute implements VOMSAttribute {

	private final String voName;

	public MappedVOMSAttribute(String voName) {

		this.voName = voName;
	}

	@Override
	public String getVO() {

		return voName;
	}

	@Override
	public String getHost() {

		return null;
	}

	@Override
	public int getPort() {

		return 0;
	}

	@Override
	public X500Principal getHolder() {

		return null;
	}

	@Override
	public BigInteger getHolderSerialNumber() {

		return null;
	}

	@Override
	public X500Principal getIssuer() {

		return null;
	}

	@Override
	public Date getNotBefore() {

		return null;
	}

	@Override
	public Date getNotAfter() {

		return null;
	}

	@Override
	public List<String> getFQANs() {

		return Arrays.asList("/" + getVO());
	}

	@Override
	public String getPrimaryFQAN() {

		return "/" + getVO();
	}

	@Override
	public byte[] getSignature() {

		return null;
	}

	@Override
	public List<VOMSGenericAttribute> getGenericAttributes() {

		return Collections.emptyList();
	}

	@Override
	public List<String> getTargets() {

		return Collections.emptyList();
	}

	@Override
	public X509Certificate[] getAACertificates() {

		return null;
	}

	@Override
	public boolean isValid() {

		return true;
	}

	@Override
	public boolean validAt(Date time) {

		return true;
	}

	@Override
	public X509AttributeCertificateHolder getVOMSAC() {

		return null;
	}

}
