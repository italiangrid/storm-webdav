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
package org.italiangrid.storm.webdav.authz.vomap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import eu.emi.security.authn.x509.impl.OpensslNameUtils;

public class MapfileVOMembershipSource implements VOMembershipSource {

  private static final Logger logger = LoggerFactory
    .getLogger(MapfileVOMembershipSource.class);

  private final String voName;
  private final File mapFile;

  public MapfileVOMembershipSource(String voName, File mapFile) {

    Objects.requireNonNull(mapFile);
    Assert.hasText(voName, "VO name must not be empty");

    this.voName = voName;
    this.mapFile = mapFile;

  }

  private CSVParser getParser() {

    try {
      return new CSVParser(new FileReader(mapFile), CSVFormat.DEFAULT);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private boolean isValidCSVRecord(CSVRecord r) {

    if (r.size() > 3) {
      logger.warn("Invalid CSVRecord: {}. Illegal size: {}", r, r.size());
      return false;
    }

    if (!r.get(0).startsWith("/")) {
      logger.warn("Invalid CSVRecord: {}. Subject does not start with / : {}",
        r, r.get(0));
      return false;
    }

    return true;
  }

  @Override
  public Set<String> getVOMembers() {

    long startTime = System.currentTimeMillis();

    Set<String> subjects = new HashSet<>();

    CSVParser parser = getParser();

    try {

      List<CSVRecord> records = parser.getRecords();

      for (CSVRecord r : records) {

        if (logger.isDebugEnabled()) {
          logger.debug("Parsed record: {} for VO {}", r, voName);
        }

        if (!isValidCSVRecord(r)) {
          /* Fix https://issues.infn.it/jira/browse/STOR-1399 */
          continue;
        }

        String subject = r.get(0);

        if (logger.isDebugEnabled()) {
          logger.debug("Parsed subject {} as member of VO {}", subject, voName);
        }

        @SuppressWarnings("deprecation")
        String rfcSubject = OpensslNameUtils.opensslToRfc2253(subject);

        if (logger.isDebugEnabled()) {
          logger.debug("Converted subject {} to rfc format {}", subject,
            rfcSubject);
        }

        subjects.add(rfcSubject);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    long totalTime = System.currentTimeMillis() - startTime;

    logger.debug("Parsing VO {} members from {} took {} msecs.", voName,
      mapFile, totalTime);

    return subjects;
  }

  @Override
  public String getVOName() {

    return voName;
  }

}
