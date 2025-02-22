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

    MapfileVOMembershipSource m = new MapfileVOMembershipSource("testers",
        new File("src/test/resources/vomsmap/testers.map"));

    Assert.assertEquals("testers", m.getVOName());
    Assert.assertTrue(m.getVOMembers().contains(AC_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains(EV_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains(COMMA_SUBJECT));
    Assert.assertTrue(m.getVOMembers().contains(RM_SUBJECT));
    Assert.assertFalse(m.getVOMembers().contains("CN=I am not Real, L=CNAF"));

  }

}
