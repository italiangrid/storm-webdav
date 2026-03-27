// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.test.authz.pdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Collections;
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

    assertTrue(violations.isEmpty());
  }

  @Test
  void testDescriptionRequired() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setDescription(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertEquals(1, violations.size());
    assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    assertEquals("must not be blank", violations.iterator().next().getMessage());
  }

  @Test
  void testSaRequired() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setSa(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertEquals(1, violations.size());
    assertEquals("sa", violations.iterator().next().getPropertyPath().toString());
    assertEquals("must not be blank", violations.iterator().next().getMessage());
  }

  @Test
  void testPrincipalsNotEmpty() {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setPrincipals(Collections.emptyList());

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertEquals(1, violations.size());
    assertEquals("principals", violations.iterator().next().getPropertyPath().toString());
    assertEquals("must not be empty", violations.iterator().next().getMessage());
  }
}
