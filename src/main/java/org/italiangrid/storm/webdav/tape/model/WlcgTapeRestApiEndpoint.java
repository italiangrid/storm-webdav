// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tape.model;

import java.net.URI;
import java.util.Map;

public record WlcgTapeRestApiEndpoint(URI uri, String version, Map<String, String> metadata) {}
