// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.security.auth.x500.X500Principal;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSGenericAttribute;
import org.italiangrid.voms.ac.impl.VOMSAttributesImpl;
import org.italiangrid.voms.ac.impl.VOMSGenericAttributeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VOMSNginxParser {

  public static final Logger LOG = LoggerFactory.getLogger(VOMSNginxParser.class);

  private VOMSNginxParser() {}

  public static List<VOMSAttribute> getAttributes(HttpServletRequest request) {
    // voms_user and voms_user_ca are present only when a VOMS proxy is used
    // After checking that they are present, use ssl_client_ee_*_dn that are formatted according to
    // RFC 2253
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
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VOMSConstants.VOMS_DATE_FORMAT);
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
              List<VOMSGenericAttribute> genericAttrs = new ArrayList<>();
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
            vomsSerialHeader -> attrs.setHolderSerialNumber(new BigInteger(vomsSerialHeader, 16)));
    return List.of(attrs);
  }
}
