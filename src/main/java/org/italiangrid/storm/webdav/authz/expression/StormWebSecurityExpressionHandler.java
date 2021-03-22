/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.authz.expression;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

@Component
public class StormWebSecurityExpressionHandler extends OAuth2WebSecurityExpressionHandler {

  @Override
  protected StandardEvaluationContext createEvaluationContextInternal(Authentication authentication,
      FilterInvocation invocation) {

    StandardEvaluationContext ec =
        super.createEvaluationContextInternal(authentication, invocation);
    ec.setVariable("storm", new StormSecurityExpressionMethods(authentication));

    return ec;
  }

}
