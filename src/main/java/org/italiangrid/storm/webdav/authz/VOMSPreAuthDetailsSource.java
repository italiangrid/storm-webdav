/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.authz;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class VOMSPreAuthDetailsSource
    implements AuthenticationDetailsSource<HttpServletRequest, VOMSAuthenticationDetails> {

  private final AuthorizationPolicyService policyService;
  private final VOMSACValidator validator;
  private final VOMapDetailsService voMapDetailsService;

  public VOMSPreAuthDetailsSource(VOMSACValidator vomsValidator,
      AuthorizationPolicyService policyService, VOMapDetailsService voMapDetailsService) {
    this.policyService = policyService;
    this.validator = vomsValidator;
    this.voMapDetailsService = voMapDetailsService;
  }

  @Override
  public VOMSAuthenticationDetails buildDetails(HttpServletRequest request) {

    Set<GrantedAuthority> authorities = Sets.newHashSet();

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

    Optional<X500Principal> principal = Utils.getX500PrincipalFromRequest(request);

    if (!principal.isPresent()) {
      return Collections.emptySet();
    }

    LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>();

    for (String voName : voMapDetailsService.getPrincipalVOs(principal.get())) {
      authorities.add(new VOMSVOMapAuthority(voName));
    }

    return authorities;
  }

  protected Set<GrantedAuthority> getAuthoritiesFromAttributes(List<VOMSAttribute> attributes) {
    if (attributes.isEmpty()) {
      return Collections.emptySet();
    }

    LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>();

    for (VOMSAttribute va : attributes) {
      authorities.add(new VOMSVOAuthority(va.getVO()));
      for (String fqan : va.getFQANs()) {
        authorities.add(new VOMSFQANAuthority(fqan));
      }
    }

    return authorities;
  }

  protected Optional<X509SubjectAuthority> getSubjectAuthority(HttpServletRequest request) {
    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);
    if (chain.isPresent()) {
      return Optional.of(new X509SubjectAuthority(
          ProxyUtils.getEndUserCertificate(chain.get()).getSubjectDN().getName()));
    }

    return Optional.empty();
  }

  protected List<VOMSAttribute> getAttributes(HttpServletRequest request) {

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent()) {
      if (chain.get().length > 0) {
        return validator.validate(chain.get());
      }
    }

    return Collections.emptyList();
  }

}
