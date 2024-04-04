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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VOMSPreAuthDetailsSource implements
    AuthenticationDetailsSource<HttpServletRequest, VOMSAuthenticationDetails>, VOMSConstants {

  public static final Logger LOG = LoggerFactory.getLogger(VOMSPreAuthDetailsSource.class);

  private final AuthorizationPolicyService policyService;
  private final VOMSACValidator validator;
  private final VOMapDetailsService voMapDetailsService;
  private final boolean nginxReverseProxy;

  public VOMSPreAuthDetailsSource(VOMSACValidator vomsValidator,
      AuthorizationPolicyService policyService, VOMapDetailsService voMapDetailsService,
      boolean nginxReverseProxy) {
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
      // voms_user and voms_user_ca are present only when a VOMS proxy is used
      // After checking that they are present, use ssl_client_ee_*_dn that are formatted according
      // to RFC 2253
      if (request.getHeader(VOMS_USER_HEADER) != null
          && request.getHeader(SSL_CLIENT_EE_S_DN_HEADER) != null) {
        attrs.setHolder(new X500Principal(request.getHeader(SSL_CLIENT_EE_S_DN_HEADER)));
      }
      if (request.getHeader(VOMS_USER_CA_HEADER) != null
          && request.getHeader(SSL_CLIENT_EE_I_DN_HEADER) != null) {
        attrs.setIssuer(new X500Principal(request.getHeader(SSL_CLIENT_EE_I_DN_HEADER)));
      }
      if (request.getHeader(VOMS_FQANS_HEADER) != null) {
        attrs.setFQANs(Arrays.asList(request.getHeader(VOMS_FQANS_HEADER).split(",")));
      }
      if (request.getHeader(VOMS_VO_HEADER) != null) {
        attrs.setVO(request.getHeader(VOMS_VO_HEADER));
      }
      if (request.getHeader(VOMS_SERVER_URI_HEADER) != null) {
        String[] splittedServerUri = request.getHeader(VOMS_SERVER_URI_HEADER).split(":");
        attrs.setHost(splittedServerUri[0]);
        attrs.setPort(Integer.parseInt(splittedServerUri[1]));
      }
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VOMS_DATE_FORMAT);
      if (request.getHeader(VOMS_NOT_BEFORE_HEADER) != null) {
        try {
          attrs.setNotBefore(simpleDateFormat.parse(request.getHeader(VOMS_NOT_BEFORE_HEADER)));
        } catch (ParseException e) {
          LOG.warn("Error parsing X-VOMS-voms_not_before header: {}",
              request.getHeader(VOMS_NOT_BEFORE_HEADER));
        }
      }
      if (request.getHeader(VOMS_NOT_AFTER_HEADER) != null) {
        try {
          attrs.setNotAfter(simpleDateFormat.parse(request.getHeader(VOMS_NOT_AFTER_HEADER)));
        } catch (ParseException e) {
          LOG.warn("Error parsing X-VOMS-voms_not_after header: {}",
              request.getHeader(VOMS_NOT_AFTER_HEADER));
        }
      }
      if (request.getHeader(VOMS_GENERIC_ATTRIBUTES_HEADER) != null) {
        List<VOMSGenericAttribute> genericAttrs = Collections.emptyList();
        Pattern pattern = Pattern.compile(VOMS_GENERIC_ATTRIBUTES_REGEX);
        for (String genericAttribute : request.getHeader(VOMS_GENERIC_ATTRIBUTES_HEADER)
          .split(",")) {
          Matcher matcher = pattern.matcher(genericAttribute);
          if (matcher.find()) {
            VOMSGenericAttributeImpl genericAttr = new VOMSGenericAttributeImpl();
            genericAttr.setName(matcher.group(1));
            genericAttr.setValue(matcher.group(2));
            genericAttr.setContext(matcher.group(3));
            genericAttrs.add(genericAttr);
          }
        }
        attrs.setGenericAttributes(genericAttrs);
      }
      if (request.getHeader(VOMS_SERIAL_HEADER) != null) {
        attrs.setHolderSerialNumber(new BigInteger(request.getHeader(VOMS_SERIAL_HEADER), 16));
      }
      return List.of(attrs);
    }

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent() && chain.get().length > 0) {
      return validator.validate(chain.get());
    }

    return Collections.emptyList();
  }

}
