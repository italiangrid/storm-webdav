package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;

public class VOMSAttributeCertificateAuthDetailsSource extends
	AbstractVOMSAuthDetailsSource implements ValidationResultListener {

	public static final long VOMS_AA_VALIDATION_CACHE_LIFETIME = TimeUnit.MINUTES
		.toMillis(5);

	private final VOMSACValidator vomsValidator;

	public VOMSAttributeCertificateAuthDetailsSource() {

		X509CertChainValidatorExt wrappedValidator = new CertificateValidatorBuilder()
			.lazyAnchorsLoading(false).ocspChecks(OCSPCheckingMode.IGNORE)
			.crlChecks(CrlCheckingMode.IF_VALID).build();

		X509CertChainValidatorExt aaCertChainValidator = new CachingCertificateValidator(
			wrappedValidator, VOMS_AA_VALIDATION_CACHE_LIFETIME);

		vomsValidator = new DefaultVOMSValidator.Builder()
			.certChainValidator(aaCertChainValidator).validationListener(this)
			.build();
	}

	protected List<VOMSAttribute> getAttributes(HttpServletRequest request) {

		Assert.notNull(request, "Cannot extract attributes from a null request!");
		X509Certificate[] chain = getClientCertificateChain(request);
		if (chain != null && chain.length > 0) {
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

	@Override
	public Collection<GrantedAuthority> getVOMSGrantedAuthorities(
		HttpServletRequest request) {

		List<VOMSAttribute> vomsAttrs = getAttributes(request);

		if (vomsAttrs.isEmpty()) {
			return Collections.emptyList();
		}

		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		for (VOMSAttribute va : vomsAttrs) {
			if (logger.isDebugEnabled()) {
				logger.debug("Adding VO authority: {}", va.getVO());
			}
			authorities.add(new VOMSVOAuthority(va.getVO()));
			for (String fqan: va.getFQANs()){
				if (logger.isDebugEnabled()){
					logger.debug("Adding FQAN authority: {}", fqan);
				}
				authorities.add(new VOMSFQANAuthority(fqan));
			}
		}

		return authorities;
	}
}
