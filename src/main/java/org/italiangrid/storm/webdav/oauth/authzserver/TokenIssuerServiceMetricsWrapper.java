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
