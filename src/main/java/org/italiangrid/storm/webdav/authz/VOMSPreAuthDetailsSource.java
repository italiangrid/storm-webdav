// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import eu.emi.security.authn.x509.proxy.ProxyUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;

public class VOMSPreAuthDetailsSource
    implements AuthenticationDetailsSource<HttpServletRequest, VOMSAuthenticationDetails> {

  public static final Logger LOG = LoggerFactory.getLogger(VOMSPreAuthDetailsSource.class);

  private final AuthorizationPolicyService policyService;
  private final VOMSACValidator validator;
  private final VOMapDetailsService voMapDetailsService;
  private final boolean nginxEnabled;

  public VOMSPreAuthDetailsSource(
      VOMSACValidator vomsValidator,
      AuthorizationPolicyService policyService,
      VOMapDetailsService voMapDetailsService,
      boolean nginxEnabled) {
    this.policyService = policyService;
    this.validator = vomsValidator;
    this.voMapDetailsService = voMapDetailsService;
    this.nginxEnabled = nginxEnabled;
  }

  @Override
  public VOMSAuthenticationDetails buildDetails(HttpServletRequest request) {

    Set<GrantedAuthority> authorities = new HashSet<>();

    List<VOMSAttribute> attributes = getAttributes(request);

    getSubjectAuthority(request).ifPresent(authorities::add);

    authorities.addAll(getAuthoritiesFromAttributes(attributes));

    if (attributes.isEmpty()) {
      authorities.addAll(getAuthoritiesFromVoMapFiles(request));
    }

    authorities.addAll(policyService.getSAPermissions(authorities));

    return new VOMSAuthenticationDetails(request, authorities, attributes);
  }

  protected Set<GrantedAuthority> getAuthoritiesFromVoMapFiles(HttpServletRequest request) {

    Optional<X500Principal> principal;
    if (nginxEnabled) {
      principal =
          Optional.ofNullable(request.getHeader(VOMSConstants.SSL_CLIENT_EE_S_DN_HEADER))
              .map(X500Principal::new);
    } else {
      principal = Utils.getX500PrincipalFromRequest(request);
    }

    if (!principal.isPresent()) {
      return Collections.emptySet();
    }

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();

    for (String voName : voMapDetailsService.getPrincipalVOs(principal.get())) {
      authorities.add(new VOMSVOMapAuthority(voName));
    }

    return authorities;
  }

  protected Set<GrantedAuthority> getAuthoritiesFromAttributes(List<VOMSAttribute> attributes) {
    if (attributes.isEmpty()) {
      return Collections.emptySet();
    }

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();

    for (VOMSAttribute va : attributes) {
      authorities.add(new VOMSVOAuthority(va.getVO()));
      for (String fqan : va.getFQANs()) {
        authorities.add(new VOMSFQANAuthority(fqan));
      }
    }

    return authorities;
  }

  protected Optional<X509SubjectAuthority> getSubjectAuthority(HttpServletRequest request) {
    if (nginxEnabled) {
      Optional.ofNullable(request.getHeader(VOMSConstants.SSL_CLIENT_EE_S_DN_HEADER))
          .ifPresent(X509SubjectAuthority::new);
    } else {
      Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);
      if (chain.isPresent()) {
        return Optional.of(
            new X509SubjectAuthority(
                ProxyUtils.getEndUserCertificate(chain.get()).getSubjectX500Principal().getName()));
      }
    }

    return Optional.empty();
  }

  protected List<VOMSAttribute> getAttributes(HttpServletRequest request) {
    if (nginxEnabled) {
      return VOMSNginxParser.getAttributes(request);
    }

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent() && chain.get().length > 0) {
      return validator.validate(chain.get());
    }

    return Collections.emptyList();
  }
}
