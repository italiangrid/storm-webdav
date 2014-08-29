package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.VOMSValidationResult;
import org.italiangrid.voms.ac.ValidationResultListener;
import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
import org.italiangrid.voms.util.CachingCertificateValidator;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.impl.CertificateUtils;
import eu.emi.security.authn.x509.impl.FormatMode;


public class DefaultVOMSAttributesExtractor implements VOMSAttributesExtractor, ValidationResultListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultVOMSAttributesExtractor.class);
	
	public static final long VOMS_AA_VALIDATION_CACHE_LIFETIME = TimeUnit.MINUTES.toMillis(5);
	
	private final VOMSACValidator vomsValidator;
	
	public DefaultVOMSAttributesExtractor() {

		X509CertChainValidatorExt wrappedValidator = new CertificateValidatorBuilder()
			.lazyAnchorsLoading(false)
			.ocspChecks(OCSPCheckingMode.IGNORE)
			.crlChecks(CrlCheckingMode.IF_VALID)
			.build();
	
		X509CertChainValidatorExt aaCertChainValidator =  new CachingCertificateValidator(wrappedValidator,
			VOMS_AA_VALIDATION_CACHE_LIFETIME);
	
		vomsValidator = new DefaultVOMSValidator.Builder()
			.certChainValidator(aaCertChainValidator)
			.validationListener(this)
			.build();
	}
	
	private X509Certificate[] getClientCertificateChain(HttpServletRequest request){
		X509Certificate[] chain= Utils.getCertificateChainFromRequest(request);
		
		if (chain != null && chain.length > 0){
			if (logger.isDebugEnabled()){		
				logger.debug("Certificate chain in incoming request: {}", 
					CertificateUtils.format(chain, FormatMode.COMPACT_ONE_LINE));
			}
			return chain;
		}
		
		logger.debug("No certificate chain in incoming request.");
		return null;
	}
	@Override
	public List<VOMSAttribute> getAttributes(HttpServletRequest request) {

		Assert.notNull(request, "Cannot extract attributes from a null request!");
		X509Certificate[] chain = getClientCertificateChain(request);
		if (chain != null && chain.length > 0){
			return vomsValidator.validate(chain);
		}
		
		return Collections.emptyList();
	}

	@Override
	public void notifyValidationResult(VOMSValidationResult result) {
		
		if (!result.isValid())
			logger.warn("VOMS attributes validation result: {}", result);
		else
			logger.debug("VOMS attributes validation result: {}", result);
		
	}

}
