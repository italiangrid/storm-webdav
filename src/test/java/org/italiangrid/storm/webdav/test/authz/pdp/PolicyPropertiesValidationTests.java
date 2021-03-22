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
package org.italiangrid.storm.webdav.test.authz.pdp;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.italiangrid.storm.webdav.authz.pdp.PolicyEffect;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties;
import org.italiangrid.storm.webdav.config.FineGrainedAuthzPolicyProperties.PrincipalProperties.PrincipalType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.Lists;

@RunWith(JUnit4.class)
public class PolicyPropertiesValidationTests {


  private Validator validator;

  @Before
  public void setup() {

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
    props.setPrincipals(Lists.newArrayList(principal));
    props.setActions(EnumSet.of(FineGrainedAuthzPolicyProperties.Action.READ));
    return props;
  }

  @Test
  public void testValidAuthzPolicyPassesValidation() throws Exception {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, empty());
    

  }
  
  @Test
  public void testDescriptionRequired() throws Exception {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setDescription(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("description"));
    assertThat(violations.iterator().next().getMessage(), is("must not be blank"));
    
  }
  
  @Test
  public void testSaRequired() throws Exception {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setSa(null);

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("sa"));
    assertThat(violations.iterator().next().getMessage(), is("must not be blank"));
    
  }
  
  @Test
  public void testPrincipalsNotEmpty() throws Exception {

    FineGrainedAuthzPolicyProperties props = minimalValidPolicy();
    props.setPrincipals(emptyList());

    Set<ConstraintViolation<FineGrainedAuthzPolicyProperties>> violations =
        validator.validate(props);

    assertThat(violations, hasSize(1));
    assertThat(violations.iterator().next().getPropertyPath().toString(), is("principals"));
    assertThat(violations.iterator().next().getMessage(), is("must not be empty"));
    
  }
  
  
  

}
