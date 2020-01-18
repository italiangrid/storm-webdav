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

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.DENY;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.NOT_APPLICABLE;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.PERMIT;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.pdp.DefaultPathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicyRepository;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.principal.Anyone;
import org.italiangrid.storm.webdav.authz.pdp.principal.AuthorityHolder;
import org.italiangrid.storm.webdav.oauth.authority.OAuthGroupAuthority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class AuthzPdpTests {

  public static final String TEST_ISSUER = "https://test.example";
  public static final String TEST2_ISSUER = "https://test2.example";

  @Mock
  PathAuthorizationPolicyRepository repo;

  @Mock
  HttpServletRequest request;

  @Mock
  Authentication authentication;

  @InjectMocks
  DefaultPathAuthorizationPdp pdp;

  @SuppressWarnings("unchecked")
  private <T> ImmutableSet<T> authorities(GrantedAuthority... authorities) {
    return (ImmutableSet<T>) ImmutableSet.copyOf(authorities);
  }

  @Before
  public void setup() {
    when(request.getServletPath()).thenReturn("/");
    when(repo.getPolicies()).thenReturn(emptyList());
  }


  @Test
  public void notApplicableWithEmptyPolicies() {

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(NOT_APPLICABLE));
  }


  @Test
  public void denyPolicyApplied() {

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));

  }

  @Test
  public void firstApplicablePolicyApplied() {

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    PathAuthorizationPolicy permitAllPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(permitAllPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(permitAllPolicy));

  }

  @Test
  public void oauthGroupHolderPolicyNotApplied() {
    when(authentication.getAuthorities()).thenReturn(emptyList());


    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new OAuthGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  public void oauthGroupHolderPolicyNotAppliedDueToWrongGroup() {

    when(authentication.getAuthorities())
      .thenReturn(authorities(new OAuthGroupAuthority(TEST_ISSUER, "/toast")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new OAuthGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  public void oauthGroupHolderPolicyNotAppliedDueToWrongIssuer() {

    when(authentication.getAuthorities())
      .thenReturn(authorities(new OAuthGroupAuthority(TEST2_ISSUER, "/test")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new OAuthGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  public void oauthGroupHolderPolicyApplied() {

    when(request.getServletPath()).thenReturn("/test/file0");
    when(request.getMethod()).thenReturn("GET");

    when(authentication.getAuthorities())
      .thenReturn(authorities(new OAuthGroupAuthority(TEST_ISSUER, "/test")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new OAuthGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = Lists.newArrayList(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(oauthTestPolicy));
  }

}
