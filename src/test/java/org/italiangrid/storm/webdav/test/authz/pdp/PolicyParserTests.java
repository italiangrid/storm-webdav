/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.List;

import org.italiangrid.storm.webdav.authz.SpringConfigurationAuthzPolicyParser;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.PolicyEffect;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy.Action;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicy.Principal.PrincipalType;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class PolicyParserTests {

  ServiceConfigurationProperties properties = new ServiceConfigurationProperties();

  SpringConfigurationAuthzPolicyParser parser =
      new SpringConfigurationAuthzPolicyParser(properties);

  @Test
  public void testNoPolicyParsing() throws Exception {
    assertThat(parser.parsePolicies(), empty());
  }
  
  
  @Test
  public void testSimplePolicyParsing() throws Exception {
    
    FineGrainedAuthzPolicy.Principal anonymous = new FineGrainedAuthzPolicy.Principal();
    anonymous.setType(PrincipalType.ANONYMOUS);
    
    FineGrainedAuthzPolicy policy = new FineGrainedAuthzPolicy();
    
    policy.setId("id");
    policy.setDescription("desc");
    policy.setEffect(PolicyEffect.DENY);
    policy.setPaths(Lists.newArrayList("/**"));
    policy.setPrincipals(Lists.newArrayList(anonymous));
    policy.setActions(EnumSet.allOf(Action.class));
    
    properties.getAuthz().setPolicies(Lists.newArrayList(policy));
    List<PathAuthorizationPolicy> policies = parser.parsePolicies();
    
    assertThat(policies, hasSize(1));
    PathAuthorizationPolicy parsedPolicy = policies.get(0);
    assertThat(parsedPolicy.getEffect(), is(PolicyEffect.DENY));
    assertThat(parsedPolicy.getId(), is("id"));
    assertThat(parsedPolicy.getDecription(), is("desc"));
    assertThat(parsedPolicy.getPrincipalMatchers(), hasSize(1));
    assertThat(parsedPolicy.getRequestMatchers(), hasSize(5));
  }
  


}
