// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.vomap;

import java.io.File;
import org.italiangrid.storm.webdav.authz.vomap.MapfileVOMembershipSource;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class VOMSMapTests {

  public static final String AC_SUBJECT =
      "CN=Andrea Ceccanti,L=CNAF,OU=Personal Certificate,O=INFN,C=IT";
  public static final String EV_SUBJECT =
      "CN=Enrico Vianello,L=CNAF,OU=Personal Certificate,O=INFN,C=IT";
  public static final String COMMA_SUBJECT =
      "CN=Federica Agostini,L=CNAF,Bologna,OU=Personal Certificate,O=INFN,C=IT";
  public static final String RM_SUBJECT =
      "CN=Roberta Miccoli,L=CNAF,OU=Personal Certificate,O=INFN,C=IT";

  @Test
  void VOMapParserTest() {

    MapfileVOMembershipSource m =
        new MapfileVOMembershipSource(
            "testers", new File("src/test/resources/vomsmap/testers.map"));

    Assert.assertEquals("testers", m.getVOName());
    Assert.assertTrue(m.getVOMembers().contains(AC_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains(EV_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains(COMMA_SUBJECT));
    Assert.assertTrue(m.getVOMembers().contains(RM_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains("CN=I am not Real, L=CNAF"));
  }
}
