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

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.DENY;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.NOT_APPLICABLE;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.Decision.PERMIT;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.VOMSFQANAuthority;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.pdp.DefaultPathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicy;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPolicyRepository;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult;
import org.italiangrid.storm.webdav.authz.pdp.principal.Anyone;
import org.italiangrid.storm.webdav.authz.pdp.principal.AuthorityHolder;
import org.italiangrid.storm.webdav.oauth.authority.JwtClientAuthority;
import org.italiangrid.storm.webdav.oauth.authority.JwtGroupAuthority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@ExtendWith(MockitoExtension.class)
public class AuthzPdpTests {

  public static final String TEST_ISSUER = "https://test.example";
  public static final String TEST2_ISSUER = "https://test2.example";
  public static final String AUTHORIZED_JWT_CLIENT_ID = "1234";
  public static final String UNAUTHORIZED_JWT_CLIENT_ID = "5678";

  @Mock
  PathAuthorizationPolicyRepository repo;

  @Mock
  HttpServletRequest request;

  @Mock
  Authentication authentication;

  @InjectMocks
  DefaultPathAuthorizationPdp pdp;

  @SuppressWarnings("unchecked")
  private <T> Set<T> authorities(GrantedAuthority... authorities) {
    return (Set<T>) Set.of(authorities);
  }

  @BeforeEach
  public void setup() {
    lenient().when(request.getServletPath()).thenReturn("/");
    lenient().when(repo.getPolicies()).thenReturn(emptyList());
  }


  @Test
  void notApplicableWithEmptyPolicies() {

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(NOT_APPLICABLE));
  }


  @Test
  void denyPolicyApplied() {

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));

  }

  @Test
  void firstApplicablePolicyApplied() {

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

    List<PathAuthorizationPolicy> policies = List.of(permitAllPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(permitAllPolicy));

  }

  @Test
  void oauthGroupHolderPolicyNotAppliedAsItDoesNotMatchPath() {

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  void oauthGroupHolderPolicyNotAppliedDueToWrongGroup() {

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST_ISSUER, "/test/subgroup")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withDescription("Allow GET on /test/** to members of /test group")
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDescription("Deny all")
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    when(request.getServletPath()).thenReturn("/test/ciccio");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  void oauthGroupHolderPolicyNotAppliedDueToWrongIssuer() {

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST2_ISSUER, "/test")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withDescription("Allow GET on /test/** to members of /test group")
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);
    when(request.getServletPath()).thenReturn("/test/ciccio");

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(denyAllPolicy));
  }

  @Test
  void oauthGroupHolderPolicyApplied() {

    when(request.getServletPath()).thenReturn("/test/file0");
    when(request.getMethod()).thenReturn("GET");

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST_ISSUER, "/test")));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(oauthTestPolicy));
  }

  @Test
  void oauthClientHolderPolicyApplied() {

    when(request.getServletPath()).thenReturn("/test/file0");
    when(request.getMethod()).thenReturn("GET");

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtClientAuthority(TEST_ISSUER, AUTHORIZED_JWT_CLIENT_ID)));

    PathAuthorizationPolicy oauthTestPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(AuthorityHolder
        .fromAuthority(new JwtClientAuthority(TEST_ISSUER, AUTHORIZED_JWT_CLIENT_ID)))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();

    PathAuthorizationPolicy denyAllPolicy = PathAuthorizationPolicy.builder()
      .withDeny()
      .withPrincipalMatcher(new Anyone())
      .withRequestMatcher(new AntPathRequestMatcher("/**"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(oauthTestPolicy, denyAllPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(oauthTestPolicy));

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtClientAuthority(TEST_ISSUER, UNAUTHORIZED_JWT_CLIENT_ID)));

    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(DENY));
  }

  @Test
  void multiplePrincipalMatchersWorkAsExpected() {

    when(request.getServletPath()).thenReturn("/test/file0");
    when(request.getMethod()).thenReturn("GET");

    PathAuthorizationPolicy multiplePrincipalsPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withPrincipalMatcher(AuthorityHolder.fromAuthority(new VOMSFQANAuthority("/test/example")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .build();


    List<PathAuthorizationPolicy> policies = List.of(multiplePrincipalsPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST_ISSUER, "/test")));

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(multiplePrincipalsPolicy));

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST_ISSUER, "/other")));

    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(NOT_APPLICABLE));

    when(authentication.getAuthorities()).thenReturn(authorities(new VOMSVOAuthority("test"),
        new VOMSFQANAuthority("/test"), new VOMSFQANAuthority("/test/example")));

    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));

    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(multiplePrincipalsPolicy));

    when(authentication.getAuthorities())
      .thenReturn(authorities(new SimpleGrantedAuthority("ANONYMOUS")));

    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));

    assertThat(result.getDecision(), is(NOT_APPLICABLE));
  }

  @Test
  void multiplePathsMatchersWorkAsExpected() {

    when(request.getServletPath()).thenReturn("/test/file0");
    when(request.getMethod()).thenReturn("GET");

    PathAuthorizationPolicy multiplePathsPolicy = PathAuthorizationPolicy.builder()
      .withPermit()
      .withPrincipalMatcher(
          AuthorityHolder.fromAuthority(new JwtGroupAuthority(TEST_ISSUER, "/test")))
      .withPrincipalMatcher(AuthorityHolder.fromAuthority(new VOMSFQANAuthority("/test/example")))
      .withRequestMatcher(new AntPathRequestMatcher("/test/**", "GET"))
      .withRequestMatcher(new AntPathRequestMatcher("/other/**", "GET"))
      .build();

    List<PathAuthorizationPolicy> policies = List.of(multiplePathsPolicy);
    when(repo.getPolicies()).thenReturn(policies);

    when(authentication.getAuthorities())
      .thenReturn(authorities(new JwtGroupAuthority(TEST_ISSUER, "/test")));

    PathAuthorizationResult result =
        pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(multiplePathsPolicy));

    when(request.getServletPath()).thenReturn("/other/file0");

    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));
    assertThat(result.getDecision(), is(PERMIT));
    assertThat(result.getPolicy().isPresent(), is(true));
    assertThat(result.getPolicy().get(), is(multiplePathsPolicy));

    when(request.getServletPath()).thenReturn("/yet-another");
    result = pdp.authorizeRequest(newAuthorizationRequest(request, authentication));

    assertThat(result.getDecision(), is(NOT_APPLICABLE));
  }
}
