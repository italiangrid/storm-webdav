// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import eu.emi.security.authn.x509.proxy.ProxyUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import javax.security.auth.x500.X500Principal;
import org.italiangrid.storm.webdav.authz.vomap.VOMapDetailsService;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSGenericAttribute;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.impl.VOMSAttributesImpl;
import org.italiangrid.voms.ac.impl.VOMSGenericAttributeImpl;
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
  private final SimpleDateFormat simpleDateFormat =
      new SimpleDateFormat(VOMSConstants.VOMS_DATE_FORMAT);

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
      // voms_user and voms_user_ca are present only when a VOMS proxy is used
      // After checking that they are present, use ssl_client_ee_*_dn that are formatted according
      // to RFC 2253
      if (request.getHeader(VOMSConstants.VOMS_USER_HEADER) == null
          || request.getHeader(VOMSConstants.VOMS_USER_CA_HEADER) == null) {
        return Collections.emptyList();
      }
      VOMSAttributesImpl attrs = new VOMSAttributesImpl();
      Optional.ofNullable(request.getHeader(VOMSConstants.SSL_CLIENT_EE_S_DN_HEADER))
          .ifPresent(
              sslClientEeSDnHeader -> attrs.setHolder(new X500Principal(sslClientEeSDnHeader)));
      Optional.ofNullable(request.getHeader(VOMSConstants.SSL_CLIENT_EE_I_DN_HEADER))
          .ifPresent(
              sslClientEeIDnHeader -> attrs.setIssuer(new X500Principal(sslClientEeIDnHeader)));
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_FQANS_HEADER))
          .ifPresent(vomsFqansHeader -> attrs.setFQANs(Arrays.asList(vomsFqansHeader.split(","))));
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_VO_HEADER)).ifPresent(attrs::setVO);
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_SERVER_URI_HEADER))
          .ifPresent(
              vomsServerUriHeader -> {
                String[] splittedServerUri = vomsServerUriHeader.split(":");
                attrs.setHost(splittedServerUri[0]);
                attrs.setPort(Integer.parseInt(splittedServerUri[1]));
              });
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_NOT_BEFORE_HEADER))
          .ifPresent(
              vomsNotBeforeHeader -> {
                try {
                  attrs.setNotBefore(simpleDateFormat.parse(vomsNotBeforeHeader));
                } catch (ParseException e) {
                  LOG.warn(
                      "Error parsing {} header: {}",
                      VOMSConstants.VOMS_NOT_BEFORE_HEADER,
                      vomsNotBeforeHeader);
                }
              });
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_NOT_AFTER_HEADER))
          .ifPresent(
              vomsNotAfterHeader -> {
                try {
                  attrs.setNotAfter(simpleDateFormat.parse(vomsNotAfterHeader));
                } catch (ParseException e) {
                  LOG.warn(
                      "Error parsing {} header: {}",
                      VOMSConstants.VOMS_NOT_AFTER_HEADER,
                      vomsNotAfterHeader);
                }
              });
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_GENERIC_ATTRIBUTES_HEADER))
          .ifPresent(
              vomsGenericAttributesHeader -> {
                List<VOMSGenericAttribute> genericAttrs = Collections.emptyList();
                for (String genericAttribute : vomsGenericAttributesHeader.split(",")) {
                  Matcher matcher =
                      VOMSConstants.VOMS_GENERIC_ATTRIBUTES_PATTERN.matcher(genericAttribute);
                  if (matcher.find()) {
                    VOMSGenericAttributeImpl genericAttr = new VOMSGenericAttributeImpl();
                    genericAttr.setName(matcher.group(1));
                    genericAttr.setValue(matcher.group(2));
                    genericAttr.setContext(matcher.group(3));
                    genericAttrs.add(genericAttr);
                  }
                }
                attrs.setGenericAttributes(genericAttrs);
              });
      Optional.ofNullable(request.getHeader(VOMSConstants.VOMS_SERIAL_HEADER))
          .ifPresent(
              vomsSerialHeader ->
                  attrs.setHolderSerialNumber(new BigInteger(vomsSerialHeader, 16)));
      return List.of(attrs);
    }

    Optional<X509Certificate[]> chain = Utils.getCertificateChainFromRequest(request);

    if (chain.isPresent() && chain.get().length > 0) {
      return validator.validate(chain.get());
    }

    return Collections.emptyList();
  }
}
