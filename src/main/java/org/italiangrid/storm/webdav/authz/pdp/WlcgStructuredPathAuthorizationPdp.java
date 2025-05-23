// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.pdp;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.deny;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.indeterminate;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationResult.permit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.italiangrid.storm.webdav.authz.util.MatcherUtils;
import org.italiangrid.storm.webdav.authz.util.StructuredPathScopeMatcher;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.italiangrid.storm.webdav.tpc.TpcUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class WlcgStructuredPathAuthorizationPdp
    implements PathAuthorizationPdp, MatcherUtils, TpcUtils {

  public static final String WLCG_STORAGE_SCOPE_PATTERN_STRING =
      "^storage.(read|modify|create|stage):(\\/.*)$";

  public static final Pattern WLCG_STORAGE_SCOPE_PATTERN =
      Pattern.compile(WLCG_STORAGE_SCOPE_PATTERN_STRING);

  public static final String SCOPE_CLAIM = "scope";

  public static final String STORAGE_STAGE = "storage.stage";
  public static final String STORAGE_READ = "storage.read";
  public static final String STORAGE_MODIFY = "storage.modify";
  public static final String STORAGE_CREATE = "storage.create";

  protected static final Set<String> READ_SCOPES = Set.of(STORAGE_READ, STORAGE_STAGE);
  protected static final Set<String> WRITE_SCOPES = Set.of(STORAGE_CREATE, STORAGE_MODIFY);
  protected static final Set<String> ALL_STORAGE_SCOPES =
      Set.of(STORAGE_READ, STORAGE_MODIFY, STORAGE_CREATE, STORAGE_STAGE);

  public static final String ERROR_INVALID_AUTHENTICATION =
      "Invalid authentication: expected a JwtAuthenticationToken object";
  public static final String ERROR_INVALID_TOKEN_NO_SCOPE =
      "Invalid token: no scope claim found in token";
  public static final String ERROR_SA_NOT_FOUND = "No storage area found matching request";
  public static final String ERROR_INSUFFICIENT_TOKEN_SCOPE = "Insufficient token scope";
  public static final String ERROR_UNSUPPORTED_METHOD_PATTERN = "Unsupported method: %s";

  public static final String ERROR_UNKNOWN_TOKEN_ISSUER = "Unknown token issuer: %s";

  protected static final Set<String> READONLY_METHODS = Set.of("GET", "PROPFIND");
  protected static final Set<String> REPLACE_METHODS = Set.of("PUT", "MKCOL");
  protected static final Set<String> MODIFY_METHODS = Set.of("PATCH", "DELETE");
  protected static final Set<String> CATCHALL_METHODS = Set.of("HEAD", "OPTIONS");

  public static final String COPY_METHOD = "COPY";
  public static final String MOVE_METHOD = "MOVE";

  protected final ServiceConfigurationProperties properties;
  protected final PathResolver pathResolver;
  protected final LocalURLService localUrlService;

  public WlcgStructuredPathAuthorizationPdp(
      ServiceConfigurationProperties props,
      PathResolver resolver,
      LocalURLService localUrlService) {
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

    return sa.accessPoints().stream()
        .filter(requestPath::startsWith)
        .findFirst()
        .map(s -> requestPath.substring(s.length()))
        .filter(s -> !s.isEmpty())
        .orElse("/");
  }

  public static Set<String> resolveWlcgScopes(JwtAuthenticationToken token) {
    Set<String> wlcgScopes =
        Stream.of(token.getToken().getClaimAsString(SCOPE_CLAIM).split(" "))
            .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageScope)
            .collect(Collectors.toSet());

    Set<String> implicitWlcgScopes =
        wlcgScopes.stream()
            .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageModifyScope)
            .map(s -> s.replace(STORAGE_MODIFY, STORAGE_CREATE))
            .collect(Collectors.toSet());

    wlcgScopes.addAll(implicitWlcgScopes);
    return wlcgScopes;
  }

  boolean filterMatcherByRequest(
      HttpServletRequest request,
      String method,
      StructuredPathScopeMatcher m,
      boolean requestedResourceExists) {

    if (CATCHALL_METHODS.contains(method)) {
      return ALL_STORAGE_SCOPES.stream().anyMatch(prefix -> prefix.equals(m.getPrefix()));
    }

    if (READONLY_METHODS.contains(method)) {
      return READ_SCOPES.contains(m.getPrefix());
    }
    if (REPLACE_METHODS.contains(method)) {
      if (requestedResourceExists) {
        return STORAGE_MODIFY.equals(m.getPrefix());
      }
      return WRITE_SCOPES.contains(m.getPrefix());
    }
    if (MODIFY_METHODS.contains(method)) {
      return STORAGE_MODIFY.equals(m.getPrefix());
    }
    if (COPY_METHOD.equals(method)) {

      if (isPullTpc(request, localUrlService)) {
        if (requestedResourceExists) {
          return STORAGE_MODIFY.equals(m.getPrefix());
        }
        return WRITE_SCOPES.contains(m.getPrefix());
      }
      return READ_SCOPES.contains(m.getPrefix());
    }

    if (MOVE_METHOD.equals(method)) {
      return STORAGE_MODIFY.equals(m.getPrefix());
    }

    throw new IllegalArgumentException(format(ERROR_UNSUPPORTED_METHOD_PATTERN, method));
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

    if (jwtAuth.getToken().getClaimAsString(SCOPE_CLAIM) == null) {
      return indeterminate(ERROR_INVALID_TOKEN_NO_SCOPE);
    }

    StorageAreaInfo sa = pathResolver.resolveStorageArea(requestPath);

    if (sa == null) {
      return indeterminate(ERROR_SA_NOT_FOUND);
    }

    final String tokenIssuer = jwtAuth.getToken().getIssuer().toString();

    if (!sa.orgs().contains(tokenIssuer)) {
      return deny(String.format(ERROR_UNKNOWN_TOKEN_ISSUER, tokenIssuer));
    }

    Set<String> wlcgScopes = resolveWlcgScopes(jwtAuth);

    List<StructuredPathScopeMatcher> scopeMatchers =
        wlcgScopes.stream()
            .filter(WlcgStructuredPathAuthorizationPdp::isWlcgStorageScope)
            .map(StructuredPathScopeMatcher::fromString)
            .toList();

    // Here we return indeterminate when no WLCG storage access scopes
    // are found in the token, so that other authz mechanism can be used to
    // return a decision
    if (scopeMatchers.isEmpty()) {
      return indeterminate(ERROR_INSUFFICIENT_TOKEN_SCOPE);
    }

    final boolean requestedResourceExists = pathResolver.pathExists(requestPath);
    final String saPath = getStorageAreaPath(requestPath, sa);

    if ("MKCOL".equals(method)) {
      scopeMatchers =
          scopeMatchers.stream()
              .filter(m -> filterMatcherByRequest(request, method, m, requestedResourceExists))
              .filter(m -> m.matchesPathIncludingParents(saPath))
              .toList();
    } else {
      scopeMatchers =
          scopeMatchers.stream()
              .filter(m -> filterMatcherByRequest(request, method, m, requestedResourceExists))
              .filter(m -> m.matchesPath(saPath))
              .toList();
    }

    if (scopeMatchers.isEmpty()) {
      return deny(ERROR_INSUFFICIENT_TOKEN_SCOPE);
    }

    return permit();
  }
}
