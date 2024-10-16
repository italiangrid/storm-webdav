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
package org.italiangrid.storm.webdav.authz.voters;

import java.util.Collection;

import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

public class MacaroonAuthzVoter implements AccessDecisionVoter<FilterInvocation> {

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return true;
  }

  @Override
  public int vote(Authentication authentication, FilterInvocation filterInvocation,
      Collection<ConfigAttribute> attributes) {
    Assert.notNull(authentication, "authentication must not be null");
    Assert.notNull(filterInvocation, "filterInvocation must not be null");
 
    if (HttpMethod.POST.name().equals(filterInvocation.getHttpRequest().getMethod())) {
      return ACCESS_GRANTED;
    }
    return ACCESS_ABSTAIN;
  }

}
