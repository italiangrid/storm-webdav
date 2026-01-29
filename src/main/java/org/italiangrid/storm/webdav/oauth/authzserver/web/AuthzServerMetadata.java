// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.oauth.authzserver.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AuthzServerMetadata(
    String issuer,
    String tokenEndpoint,
    List<String> responseTypesSupported,
    List<String> grantTypesSupported,
    List<String> tokenEndpointAuthMethodsSupported) {}
