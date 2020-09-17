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
package org.italiangrid.storm.webdav.test.authz.pdp;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PolicyParserTests {

  ServiceConfigurationProperties properties = new ServiceConfigurationProperties();

  @Mock
  StorageAreaConfiguration saConfig;

  @Mock
  StorageAreaInfo saInfo;
  
  FineGrainedAuthzPolicyParser parser;

  @Before
  public void setup() {
    when(saConfig.getStorageAreaInfo()).thenReturn(newArrayList(saInfo));
    when(saInfo.name()).thenReturn("test");
    when(saInfo.accessPoints()).thenReturn(newArrayList("/test"));
    
    parser = new FineGrainedAuthzPolicyParser(properties, saConfig);
  }
  
  @Test
  public void testNoPolicyParsing() throws Exception {
    assertThat(parser.parsePolicies(), empty());
  }


  @Test
  public void testSimplePolicyParsing() throws Exception {

    FineGrainedAuthzPolicyProperties.PrincipalProperties anonymous =
        new FineGrainedAuthzPolicyProperties.PrincipalProperties();
    anonymous.setType(PrincipalType.ANONYMOUS);

    FineGrainedAuthzPolicyProperties policy = new FineGrainedAuthzPolicyProperties();

    policy.setDescription("desc");
    policy.setSa("test");
    policy.setEffect(PolicyEffect.DENY);
    policy.setPrincipals(Lists.newArrayList(anonymous));
    policy.setActions(EnumSet.allOf(Action.class));

    properties.getAuthz().setPolicies(Lists.newArrayList(policy));
    List<PathAuthorizationPolicy> policies = parser.parsePolicies();

    assertThat(policies, hasSize(1));
    PathAuthorizationPolicy parsedPolicy = policies.get(0);
    assertThat(parsedPolicy.getEffect(), is(PolicyEffect.DENY));
    assertThat(parsedPolicy.getDecription(), is("desc"));
    assertThat(parsedPolicy.getPrincipalMatchers(), hasSize(1));
    assertThat(parsedPolicy.getRequestMatchers(), hasSize(5));
  }

}
