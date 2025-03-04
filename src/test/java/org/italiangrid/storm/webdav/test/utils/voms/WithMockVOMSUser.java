// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.utils.voms;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RUNTIME)
@WithSecurityContext(factory = WithMockVOMSUserSecurityContextFactory.class)
public @interface WithMockVOMSUser {

  String subject() default "/CN=test";
  String[] vos() default {"test.vo"};

  String[] saReadPermissions() default {};

  String[] saWritePermissions() default {};

  int acExpirationSecs() default 100;
  
}
