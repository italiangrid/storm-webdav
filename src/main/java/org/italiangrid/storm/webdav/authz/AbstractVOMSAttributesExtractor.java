package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;

public abstract class AbstractVOMSAttributesExtractor implements
	VOMSAttributesExtractor {

	public static final Logger logger = LoggerFactory
		.getLogger(AbstractVOMSAttributesExtractor.class);

	protected AbstractVOMSAttributesExtractor() {

	}

	protected X509Certificate[] getClientCertificateChain(
		HttpServletRequest request) {

		X509Certificate[] chain = Utils.getCertificateChainFromRequest(request);

		if (chain != null && chain.length > 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("Certificate chain in incoming request: {}",
					CertificateUtils.format(chain, FormatMode.COMPACT_ONE_LINE));
			}
			return chain;
		}

		logger.debug("No certificate chain in incoming request.");
		return null;
	}

}
