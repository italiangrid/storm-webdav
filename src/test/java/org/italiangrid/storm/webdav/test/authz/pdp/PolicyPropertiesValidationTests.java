// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.pdp;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.italiangrid.storm.webdav.authz.pdp.PolicyEffect;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyPropertiesValidationTests {

  private Validator validator;

  @BeforeEach
  void setup() {
    Locale.setDefault(Locale.ENGLISH);
    ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
    this.validator = vf.getValidator();
  }

  public FineGrainedAuthzPolicyProperties minimalValidPolicy() {

    PrincipalProperties principal = new FineGrainedAuthzPolicyProperties.PrincipalProperties();
    principal.setType(PrincipalType.ANY_AUTHENTICATED_USER);

    FineGrainedAuthzPolicyProperties props = new FineGrainedAuthzPolicyProperties();
    props.setSa("example");
    props.setDescription("Policy description");
    props.setEffect(PolicyEffect.PERMIT);
    props.setPrincipals(List.of(principal));
    props.setActions(EnumSet.of(FineGrainedAuthzPolicyProperties.Action.READ));
    return props;
  }

  @Test
  void testValidAuthzPolicyPassesValidation() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, empty());
  }

  @Test
  void testDescriptionRequired() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setDescription(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("description"));
    assertThat(violations.iterator().next().getMessage(), is("must not be blank"));
  }

  @Test
  void testSaRequired() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setSa(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("sa"));
    assertThat(violations.iterator().next().getMessage(), is("must not be blank"));
  }

  @Test
  void testPrincipalsNotEmpty() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setPrincipals(emptyList());

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("principals"));
    assertThat(violations.iterator().next().getMessage(), is("must not be empty"));
  }
}
