// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tape.model;

import java.util.List;

public record WlcgTapeRestApi(
    String sitename, String description, List<WlcgTapeRestApiEndpoint> endpoints) {}
