// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.italiangrid.storm.webdav.authz.pdp.PolicyEffect;
import org.italiangrid.storm.webdav.config.validation.Principal;
import org.springframework.validation.annotation.Validated;

@Validated
public class FineGrainedAuthzPolicyProperties {

  @Valid
  @Principal
  public static class PrincipalProperties {

    public enum PrincipalType {
      ANONYMOUS,
      ANY_AUTHENTICATED_USER,
      ANYONE,
      JWT_GROUP,
      JWT_SCOPE,
      JWT_ISSUER,
      JWT_SUBJECT,
      JWT_CLIENT,
      VO,
      FQAN,
      VO_MAP,
      X509_SUBJECT
    }

    PrincipalType type;

    Map<String, String> params = new HashMap<>();

    public PrincipalType getType() {
      return type;
    }

    public void setType(PrincipalType type) {
      this.type = type;
    }

    public Map<String, String> getParams() {
      return params;
    }

    public void setParams(Map<String, String> params) {
      this.params = params;
    }
  }

  public enum Action {
    READ,
    WRITE,
    DELETE,
    LIST,
    ALL
  }

  @NotBlank String description;

  PolicyEffect effect = PolicyEffect.DENY;

  @NotBlank String sa;

  List<String> paths = new ArrayList<>();

  @NotEmpty @Valid List<PrincipalProperties> principals;

  @NotEmpty EnumSet<Action> actions;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PolicyEffect getEffect() {
    return effect;
  }

  public void setEffect(PolicyEffect effect) {
    this.effect = effect;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPaths(List<String> paths) {
    this.paths = paths;
  }

  public List<PrincipalProperties> getPrincipals() {
    return principals;
  }

  public void setPrincipals(List<PrincipalProperties> principals) {
    this.principals = principals;
  }

  public Set<Action> getActions() {
    return actions;
  }

  public void setActions(EnumSet<Action> actions) {
    this.actions = actions;
  }

  public String getSa() {
    return sa;
  }

  public void setSa(String sa) {
    this.sa = sa;
  }
}
