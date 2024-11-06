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
package org.italiangrid.storm.webdav.config.validation;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.FQAN;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_GROUP;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_SCOPE;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_ISSUER;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.JWT_SUBJECT;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.VO;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.VO_MAP;
import static org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType.X509_SUBJECT;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;

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

      if (isNullOrEmpty(value.getParams().get(ra))) {
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
