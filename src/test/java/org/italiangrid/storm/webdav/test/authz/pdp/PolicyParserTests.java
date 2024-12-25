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
package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.lenient;

import java.util.EnumSet;
import java.util.List;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.PolicyEffect;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyParser;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.Action;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyParserTests {

  ServiceConfigurationProperties properties = new ServiceConfigurationProperties();

  @Mock
  StorageAreaConfiguration saConfig;

  @Mock
  StorageAreaInfo saInfo;

  FineGrainedAuthzPolicyParser parser;

  @BeforeEach
  public void setup() {
    lenient().when(saConfig.getStorageAreaInfo()).thenReturn(List.of(saInfo));
    lenient().when(saInfo.name()).thenReturn("test");
    lenient().when(saInfo.accessPoints()).thenReturn(List.of("/test"));

    parser = new FineGrainedAuthzPolicyParser(properties, saConfig);
  }

  @Test
  void testNoPolicyParsing() {
    assertThat(parser.parsePolicies(), empty());
  }


  @Test
  void testSimplePolicyParsing() {

    FineGrainedAuthzPolicyProperties.PrincipalProperties anonymous =
        new FineGrainedAuthzPolicyProperties.PrincipalProperties();
    anonymous.setType(PrincipalType.ANONYMOUS);

    FineGrainedAuthzPolicyProperties policy = new FineGrainedAuthzPolicyProperties();

    policy.setDescription("desc");
    policy.setSa("test");
    policy.setEffect(PolicyEffect.DENY);
    policy.setPrincipals(List.of(anonymous));
    policy.setActions(EnumSet.allOf(Action.class));

    properties.getAuthz().setPolicies(List.of(policy));
    List<PathAuthorizationPolicy> policies = parser.parsePolicies();

    assertThat(policies, hasSize(1));
    PathAuthorizationPolicy parsedPolicy = policies.get(0);
    assertThat(parsedPolicy.getEffect(), is(PolicyEffect.DENY));
    assertThat(parsedPolicy.getDecription(), is("desc"));
    assertThat(parsedPolicy.getPrincipalMatchers(), hasSize(1));
    assertThat(parsedPolicy.getRequestMatchers(), hasSize(5));
  }

}
