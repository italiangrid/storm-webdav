// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config.validation;

import static java.lang.String.format;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.FQAN;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_CLIENT;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_GROUP;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_SCOPE;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_ISSUER;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_SUBJECT;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.VO;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.VO_MAP;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.X509_SUBJECT;

import java.util.Collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public class PrincipalValidator implements
    ConstraintValidator<Principal, FineGrainedAuthzPolicyProperties.PrincipalProperties> {

  public static final Multimap<PrincipalType, String> REQUIRED_ARGS =
      ImmutableMultimap.<FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType, String>builder()
        .put(FQAN, "fqan")
        .put(JWT_GROUP, "iss")
        .put(JWT_GROUP, "group")
        .put(JWT_SCOPE, "iss")
        .put(JWT_SCOPE, "scope")
        .put(JWT_SUBJECT, "iss")
        .put(JWT_SUBJECT, "sub")
        .put(JWT_ISSUER, "iss")
        .put(JWT_CLIENT, "iss")
        .put(JWT_CLIENT, "id")
        .put(VO, "vo")
        .put(VO_MAP, "vo")
        .put(X509_SUBJECT, "subject")
        .build();


  @Override
  public boolean isValid(
      org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties value,
      ConstraintValidatorContext context) {

    Collection<String> requiredArgs = REQUIRED_ARGS.get(value.getType());

    if (requiredArgs == null || requiredArgs.isEmpty()) {
      return true;
    }

    for (String ra : requiredArgs) {
      if (!value.getParams().containsKey(ra)) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(format("Required param '%s' not found", ra))
          .addConstraintViolation();
        return false;
      }

      if (!StringUtils.hasText(value.getParams().get(ra))) {
        context.disableDefaultConstraintViolation();
        context
          .buildConstraintViolationWithTemplate(
              format("Required param '%s' value is null or empty", ra))
          .addConstraintViolation();
        return false;
      }
    }

    return true;
  }

}
