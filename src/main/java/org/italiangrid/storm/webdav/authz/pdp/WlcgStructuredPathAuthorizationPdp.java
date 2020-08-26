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
package org.italiangrid.storm.webdav.authz.pdp;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.deny;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.indeterminate;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.permit;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.authz.util.StructuredPathScopeMatcher;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.google.common.collect.Sets;

public class WlcgStructuredPathAuthorizationPdp
    implements PathAuthorizationPdp, MatcherUtils, TpcUtils {

  public static final String WLCG_STORAGE_SCOPE_PATTERN_STRING =
      "^storage.(read|modify|create):(\\/.*)$";

  public static final Pattern WLCG_STORAGE_SCOPE_PATTERN =
      Pattern.compile(WLCG_STORAGE_SCOPE_PATTERN_STRING);

  public static final String SCOPE_CLAIM = "scope";

  public static final String STORAGE_READ = "storage.read";
  public static final String STORAGE_MODIFY = "storage.modify";
  public static final String STORAGE_CREATE = "storage.create";

  public static final String ERROR_INVALID_AUTHENTICATION =
      "Invalid authentication: expected a JwtAuthenticationToken object";
  public static final String ERROR_INVALID_TOKEN_NO_SCOPE =
      "Invalid token: no scope claim found in token";
  public static final String ERROR_SA_NOT_FOUND = "No storage area found matching request";
  public static final String ERROR_INSUFFICIENT_TOKEN_SCOPE = "Insufficient token scope";
  public static final String ERROR_UNSUPPORTED_METHOD_PATTERN = "Unsupported method: %s";

  public static final Set<String> READONLY_METHODS =
      Sets.newHashSet("GET", "OPTIONS", "HEAD", "PROPFIND");

  public static final Set<String> REPLACE_METHODS = Sets.newHashSet("PUT", "MKCOL");

  public static final Set<String> MODIFY_METHODS = Sets.newHashSet("PATCH", "DELETE");

  public static final String COPY_METHOD = "COPY";
  public static final String MOVE_METHOD = "MOVE";

  protected final ServiceConfigurationProperties properties;
  protected final PathResolver pathResolver;
  protected final LocalURLService localUrlService;

  public WlcgStructuredPathAuthorizationPdp(ServiceConfigurationProperties props,
      PathResolver resolver, LocalURLService localUrlService) {
    this.properties = props;
    this.pathResolver = resolver;
    this.localUrlService = localUrlService;
  }

  public static boolean isWlcgStorageModifyScope(String scope) {
    return WLCG_STORAGE_SCOPE_PATTERN.matcher(scope).matches() && scope.startsWith(STORAGE_MODIFY);
  }

  public static boolean isWlcgStorageScope(String scope) {
    return WLCG_STORAGE_SCOPE_PATTERN.matcher(scope).matches();
  }

  public String getStorageAreaPath(String requestPath, StorageAreaInfo sa) {

    return sa.accessPoints()
      .stream()
      .filter(requestPath::startsWith)
      .findFirst()
      .map(s -> requestPath.substring(s.length()))
      .filter(s -> !s.isEmpty())
      .orElse("/");
  }

  public static Set<String> resolveWlcgScopes(JwtAuthenticationToken token) {
    Set<String> wlcgScopes = Stream.of(token.getToken().getClaimAsString(SCOPE_CLAIM).split(" "))
      .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageScope)
      .collect(Collectors.toSet());

    Set<String> implicitWlcgScopes = wlcgScopes.stream()
      .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageModifyScope)
      .map(s -> s.replace(STORAGE_MODIFY, STORAGE_CREATE))
      .collect(Collectors.toSet());

    wlcgScopes.addAll(implicitWlcgScopes);
    return wlcgScopes;
  }

  boolean filterMatcherByRequest(HttpServletRequest request, String method,
      StructuredPathScopeMatcher m, boolean requestedResourceExists) {

    String requiredScope = null;

    if (READONLY_METHODS.contains(method)) {
      requiredScope = STORAGE_READ;
    } else if (REPLACE_METHODS.contains(method)) {
      if (requestedResourceExists) {
        requiredScope = STORAGE_MODIFY;
      } else {
        requiredScope = STORAGE_CREATE;
      }
    } else if (MODIFY_METHODS.contains(method)) {
      requiredScope = STORAGE_MODIFY;
    } else if (COPY_METHOD.equals(method)) {

      requiredScope = STORAGE_READ;

      if (isPullTpc(request, localUrlService)) {
        if (requestedResourceExists) {
          requiredScope = STORAGE_MODIFY;
        } else {
          requiredScope = STORAGE_CREATE;
        }
      }

    } else if (MOVE_METHOD.equals(method)) {
      requiredScope = STORAGE_MODIFY;
    }

    if (isNull(requiredScope)) {
      throw new IllegalArgumentException(format(ERROR_UNSUPPORTED_METHOD_PATTERN, method));
    }

    return m.getPrefix().equals(requiredScope);
  }

  @Override
  public PathAuthorizationResult authorizeRequest(PathAuthorizationRequest authzRequest) {

    final HttpServletRequest request = authzRequest.getRequest();
    final Authentication authentication = authzRequest.getAuthentication();
    final String requestPath =
        Optional.ofNullable(authzRequest.getPath()).orElse(getRequestPath(request));

    final String method = Optional.ofNullable(authzRequest.getMethod()).orElse(request.getMethod());

    if (!(authentication instanceof JwtAuthenticationToken)) {
      throw new IllegalArgumentException(ERROR_INVALID_AUTHENTICATION);
    }

    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;

    if (isNull(jwtAuth.getToken().getClaimAsString(SCOPE_CLAIM))) {
      return indeterminate(ERROR_INVALID_TOKEN_NO_SCOPE);
    }

    StorageAreaInfo sa = pathResolver.resolveStorageArea(requestPath);

    if (isNull(sa)) {
      return indeterminate(ERROR_SA_NOT_FOUND);
    }

    Set<String> wlcgScopes = resolveWlcgScopes(jwtAuth);

    List<StructuredPathScopeMatcher> scopeMatchers = wlcgScopes.stream()
      .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageScope)
      .map(StructuredPathScopeMatcher::fromString)
      .collect(toList());

    // Here we return indeterminate when no WLCG storage access scopes
    // are found in the token, so that other authz mechanism can be used to
    // return a decision
    if (scopeMatchers.isEmpty()) {
      return indeterminate(ERROR_INSUFFICIENT_TOKEN_SCOPE);
    }

    final boolean requestedResourceExists = pathResolver.pathExists(requestPath);
    final String saPath = getStorageAreaPath(requestPath, sa);

    scopeMatchers = scopeMatchers.stream()
      .filter(m -> filterMatcherByRequest(request, method, m, requestedResourceExists))
      .filter(m -> m.matchesPath(saPath))
      .collect(toList());

    if (scopeMatchers.isEmpty()) {
      return deny(ERROR_INSUFFICIENT_TOKEN_SCOPE);
    }

    return permit();
  }

}
