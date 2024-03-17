/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
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

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSGenericAttribute;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.impl.VOMSAttributesImpl;
import org.italiangrid.voms.ac.impl.VOMSGenericAttributeImpl;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;

import com.google.common.collect.Sets;

import eu.emi.security.authn.x509.proxy.ProxyUtils;

public class VOMSPreAuthDetailsSource
    implements AuthenticationDetailsSource<HttpServletRequest, VOMSAuthenticationDetails> {

  private final AuthorizationPolicyService policyService;
  private final VOMSACValidator validator;
  private final VOMapDetailsService voMapDetailsService;
  private final boolean nginxReverseProxy;

  public VOMSPreAuthDetailsSource(VOMSACValidator vomsValidator,
      AuthorizationPolicyService policyService, VOMapDetailsService voMapDetailsService, boolean nginxReverseProxy) {
    this.policyService = policyService;
    this.validator = vomsValidator;
    this.voMapDetailsService = voMapDetailsService;
    this.nginxReverseProxy = nginxReverseProxy;
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
    if (nginxReverseProxy) {
      VOMSAttributesImpl attrs = new VOMSAttributesImpl();
      attrs.setVO(request.getHeader("X-VOMS-voms_vo"));
      if (request.getHeader("X-VOMS-voms_user_ca") != null) {
        attrs.setIssuer(new X500Principal(request.getHeader("X-VOMS-ssl_client_ee_i_dn"))); // ???
      }
      attrs.setFQANs(Arrays.asList(request.getHeader("X-VOMS-voms_fqans").split(",")));
      attrs.setHost(request.getHeader("X-VOMS-voms_server_uri").split(":")[0]);
      attrs.setPort(Integer.parseInt(request.getHeader("X-VOMS-voms_server_uri").split(":")[1]));
      if (request.getHeader("X-VOMS-voms_user") != null) {
        attrs.setHolder(new X500Principal(request.getHeader("X-VOMS-ssl_client_ee_s_dn"))); // ???
      }
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
      try {
        attrs.setNotAfter(simpleDateFormat.parse(request.getHeader("X-VOMS-voms_not_after")));
        attrs.setNotBefore(simpleDateFormat.parse(request.getHeader("X-VOMS-voms_not_before")));
      } catch (ParseException e) {}
      //attrs.setSignature();
      if (request.getHeader("X-VOMS-voms_generic_attributes") != null) {
        List<VOMSGenericAttribute> generic_attrs = Collections.emptyList();
        Pattern pattern = Pattern.compile("n=(\\S*) v=(\\S*) q=(\\S*)");
        for (String genericAttribute : request.getHeader("X-VOMS-voms_generic_attributes").split(",")) {
          Matcher matcher = pattern.matcher(genericAttribute);
          if (matcher.find()) {
            VOMSGenericAttributeImpl generic_attr = new VOMSGenericAttributeImpl();
            generic_attr.setName(matcher.group(1));
            generic_attr.setValue(matcher.group(2));
            generic_attr.setContext(matcher.group(3));
            generic_attrs.add(generic_attr);
          }
        }
        attrs.setGenericAttributes(generic_attrs);
      }
      //attrs.setTargets();
      //attrs.setAACertificates();
      //attrs.setVOMSAC();
      attrs.setHolderSerialNumber(new BigInteger(request.getHeader("X-VOMS-voms_serial"), 16));
      return List.of(attrs);
    }

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent() && chain.get().length > 0) {
      return validator.validate(chain.get());
    }

    return Collections.emptyList();
  }

}
