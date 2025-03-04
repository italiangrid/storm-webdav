// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.expression;

import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class StormMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {


  @Override
  public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication,
      MethodInvocation mi) {

    EvaluationContext ec = super.createEvaluationContext(authentication, mi);
    ec.setVariable("storm", new StormSecurityExpressionMethods(authentication.get()));

    return ec;
  }

}
