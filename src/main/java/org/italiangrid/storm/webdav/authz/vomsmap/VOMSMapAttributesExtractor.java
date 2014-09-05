package org.italiangrid.storm.webdav.authz.vomsmap;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.AbstractVOMSAttributesExtractor;
import org.italiangrid.voms.VOMSAttribute;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class VOMSMapAttributesExtractor extends AbstractVOMSAttributesExtractor {

	private final VOMSMapDetailsService mapService;

	public VOMSMapAttributesExtractor(VOMSMapDetailsService mds) {

		mapService = mds;
	}

	
	protected List<VOMSAttribute> voNamesToAttributes(Set<String> voNames) {

		if (voNames == null || voNames.isEmpty())
			return Collections.emptyList();
		
		List<VOMSAttribute> attrs = new ArrayList<VOMSAttribute>();
		for (String name: voNames){
			attrs.add(new MappedVOMSAttribute(name));
		}
		
		return attrs;
	}

	@Override
	public List<VOMSAttribute> getAttributes(HttpServletRequest request) {

		X509Certificate[] chain = getClientCertificateChain(request);

		if (chain == null) {
			return Collections.emptyList();
		}

		return voNamesToAttributes(mapService.getPrincipalVOs(ProxyUtils
			.getOriginalUserDN(chain)));
	}

}
