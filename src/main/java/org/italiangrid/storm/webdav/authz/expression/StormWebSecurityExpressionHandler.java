// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.expression;

import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.stereotype.Component;

@Component
public class StormWebSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {

  @Override
  protected StandardEvaluationContext createEvaluationContextInternal(
      Authentication authentication, FilterInvocation invocation) {

    StandardEvaluationContext ec =
        super.createEvaluationContextInternal(authentication, invocation);
    ec.setVariable("storm", new StormSecurityExpressionMethods(authentication));

    return ec;
  }
}
