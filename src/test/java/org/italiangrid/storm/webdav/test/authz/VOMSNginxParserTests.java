// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.italiangrid.storm.webdav.authz.VOMSConstants;
import org.italiangrid.storm.webdav.authz.VOMSNginxParser;
import org.italiangrid.voms.VOMSAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VOMSNginxParserTests {

  @Mock HttpServletRequest request;

  @Test
  void emptyAttributesListIfVomsUserHeaderIsNotPresent() {
    when(request.getHeader(VOMSConstants.VOMS_USER_HEADER)).thenReturn(null);

    List<VOMSAttribute> result = VOMSNginxParser.getAttributes(request);
    assertTrue(result.isEmpty());
  }

  @Test
  void emptyAttributesListIfVomsUserCAHeaderIsNotPresent() {
    when(request.getHeader(VOMSConstants.VOMS_USER_HEADER)).thenReturn("/C=IT/O=IGI/CN=test0");
    when(request.getHeader(VOMSConstants.VOMS_USER_CA_HEADER)).thenReturn(null);

    List<VOMSAttribute> result = VOMSNginxParser.getAttributes(request);
    assertTrue(result.isEmpty());
  }

  @Test
  void parseAttributesNginxVomsHeaders() {
    when(request.getHeader(VOMSConstants.VOMS_USER_HEADER)).thenReturn("/C=IT/O=IGI/CN=test0");
    when(request.getHeader(VOMSConstants.VOMS_USER_CA_HEADER)).thenReturn("/C=IT/O=IGI/CN=Test CA");
    when(request.getHeader(VOMSConstants.SSL_CLIENT_EE_S_DN_HEADER))
        .thenReturn("CN=test0,O=IGI,C=IT");
    when(request.getHeader(VOMSConstants.SSL_CLIENT_EE_I_DN_HEADER))
        .thenReturn("CN=Test CA,O=IGI,C=IT");
    when(request.getHeader(VOMSConstants.VOMS_FQANS_HEADER))
        .thenReturn("/test.vo/exp1,/test.vo/exp2,/test.vo/exp3/Role=PIPPO");
    when(request.getHeader(VOMSConstants.VOMS_VO_HEADER)).thenReturn("test.vo");
    when(request.getHeader(VOMSConstants.VOMS_SERVER_URI_HEADER)).thenReturn("voms.example:15000");
    when(request.getHeader(VOMSConstants.VOMS_NOT_BEFORE_HEADER)).thenReturn("20180101000000Z");
    when(request.getHeader(VOMSConstants.VOMS_NOT_AFTER_HEADER)).thenReturn("20180101120000Z");
    when(request.getHeader(VOMSConstants.VOMS_GENERIC_ATTRIBUTES_HEADER))
        .thenReturn("n=nickname v=newland q=test.vo,n=nickname v=giaco q=test.vo");
    when(request.getHeader(VOMSConstants.VOMS_SERIAL_HEADER)).thenReturn("7B");

    List<VOMSAttribute> result = VOMSNginxParser.getAttributes(request);
    assertFalse(result.isEmpty());
    assertEquals(new X500Principal("CN=test0,O=IGI,C=IT"), result.get(0).getHolder());
    assertEquals(new X500Principal("CN=Test CA,O=IGI,C=IT"), result.get(0).getIssuer());
    assertEquals(
        List.of("/test.vo/exp1", "/test.vo/exp2", "/test.vo/exp3/Role=PIPPO"),
        result.get(0).getFQANs());
    assertEquals("test.vo", result.get(0).getVO());
    assertEquals("voms.example", result.get(0).getHost());
    assertEquals(15000, result.get(0).getPort());
    assertEquals(Instant.parse("2018-01-01T00:00:00Z"), result.get(0).getNotBefore().toInstant());
    assertEquals(Instant.parse("2018-01-01T12:00:00Z"), result.get(0).getNotAfter().toInstant());
    assertEquals("nickname", result.get(0).getGenericAttributes().get(0).getName());
    assertEquals("newland", result.get(0).getGenericAttributes().get(0).getValue());
    assertEquals("test.vo", result.get(0).getGenericAttributes().get(0).getContext());
    assertEquals("nickname", result.get(0).getGenericAttributes().get(1).getName());
    assertEquals("giaco", result.get(0).getGenericAttributes().get(1).getValue());
    assertEquals("test.vo", result.get(0).getGenericAttributes().get(1).getContext());
    assertEquals(BigInteger.valueOf(0x7B), result.get(0).getHolderSerialNumber());
  }
}
