// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver;

import static com.codahale.metrics.MetricRegistry.name;

import org.springframework.security.core.Authentication;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class TokenIssuerServiceMetricsWrapper implements TokenIssuerService {

  public static final String METRIC_PREFIX = "token-issuer";

  private final TokenIssuerService delegate;
  private final Timer timer;
  private final Meter meter;

  public TokenIssuerServiceMetricsWrapper(TokenIssuerService delegate, MetricRegistry registry) {
    this.delegate = delegate;
    timer = registry.timer(name(METRIC_PREFIX, "requests"));
    meter = registry.meter(name(METRIC_PREFIX, "requests.count"));
  }

  @Override
  public TokenResponseDTO createAccessToken(AccessTokenRequest tokenRequest,
      Authentication authentication) {

    final Timer.Context context = timer.time();

    try {
      return delegate.createAccessToken(tokenRequest, authentication);
    } finally {
      meter.mark();
      context.stop();
    }
  }

}
