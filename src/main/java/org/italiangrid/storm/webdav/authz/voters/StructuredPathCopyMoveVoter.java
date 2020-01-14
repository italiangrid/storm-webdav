package org.italiangrid.storm.webdav.authz.voters;

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.server.servlet.WebDAVMethod.COPY;

import java.net.MalformedURLException;
import java.util.Collection;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.FilterInvocation;

public class StructuredPathCopyMoveVoter extends StructuredAuthzPathVoterSupport {

  protected final LocalURLService localUrlService;

  public StructuredPathCopyMoveVoter(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp);
    this.localUrlService = localUrlService;
  }


  @Override
  public int vote(Authentication authentication, FilterInvocation filter,
      Collection<ConfigAttribute> attributes) {

    if (!(authentication instanceof JwtAuthenticationToken)) {
      return ACCESS_ABSTAIN;
    }

    JwtAuthenticationToken authToken = (JwtAuthenticationToken) authentication;

    if (!isCopyOrMoveRequest(filter.getRequest())) {
      return ACCESS_ABSTAIN;
    }

    String destination = filter.getRequest().getHeader(DESTINATION_HEADER);

    if (destination == null) {
      return ACCESS_ABSTAIN;
    }

    if (COPY.name().equals(filter.getRequest().getMethod())
        && requestHasRemoteDestinationHeader(filter.getRequest(), localUrlService)) {
      return ACCESS_ABSTAIN;
    }

    try {

      String destinationPath = getSanitizedPathFromUrl(destination);
      StorageAreaInfo sa = resolver.resolveStorageArea(destinationPath);

      if (isNull(sa)) {
        return ACCESS_DENIED;
      }

      if (!sa.wlcgStructuredScopeAuthzEnabled()) {
        return ACCESS_ABSTAIN;
      }

      return renderDecision(PathAuthorizationRequest
        .newAuthorizationRequest(filter.getHttpRequest(), authToken, destinationPath));


    } catch (MalformedURLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
