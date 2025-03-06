// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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

  @Mock StorageAreaConfiguration saConfig;

  @Mock StorageAreaInfo saInfo;

  FineGrainedAuthzPolicyParser parser;

  @BeforeEach
  void setup() {
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
