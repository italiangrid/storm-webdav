package org.italiangrid.storm.webdav.authz.voters;

import static java.util.Objects.isNull;
import static org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationRequest.newAuthorizationRequest;

import java.util.Collection;

import org.italiangrid.storm.webdav.authz.pdp.PathAuthorizationPdp;
import org.italiangrid.storm.webdav.config.ServiceConfigurationProperties;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.server.PathResolver;
import org.italiangrid.storm.webdav.tpc.LocalURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;


public class FineGrainedAuthzVoter extends PathAuthzPdpVoterSupport {

  public static final Logger LOG = LoggerFactory.getLogger(FineGrainedAuthzVoter.class);

  public FineGrainedAuthzVoter(ServiceConfigurationProperties config, PathResolver resolver,
      PathAuthorizationPdp pdp, LocalURLService localUrlService) {
    super(config, resolver, pdp, localUrlService, true);
  }

  @Override
  public int vote(Authentication authentication, FilterInvocation filter,
      Collection<ConfigAttribute> attributes) {

    final String requestPath = getRequestPath(filter.getHttpRequest());
    StorageAreaInfo sa = resolver.resolveStorageArea(requestPath);

    if (isNull(sa) || Boolean.FALSE.equals(sa.fineGrainedAuthzEnabled())) {
      return ACCESS_ABSTAIN;
    }

    return renderDecision(newAuthorizationRequest(filter.getHttpRequest(), authentication), LOG);

  }

}
