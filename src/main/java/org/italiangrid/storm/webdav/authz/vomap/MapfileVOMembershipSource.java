// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.authz.vomap;

import eu.emi.security.authn.x509.impl.OpensslNameUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class MapfileVOMembershipSource implements VOMembershipSource {

  private static final Logger logger = LoggerFactory.getLogger(MapfileVOMembershipSource.class);

  private final String voName;
  private final File mapFile;

  public MapfileVOMembershipSource(String voName, File mapFile) {

    Objects.requireNonNull(mapFile);
    Assert.hasText(voName, "VO name must not be empty");

    this.voName = voName;
    this.mapFile = mapFile;
  }

  private boolean isValidCSVRecord(CSVRecord r) {

    if (r.size() > 3) {
      logger.warn("Invalid CSVRecord: {}. Illegal size: {}", r, r.size());
      return false;
    }

    if (!r.get(0).startsWith("/")) {
      logger.warn("Invalid CSVRecord: {}. Subject does not start with / : {}", r, r.get(0));
      return false;
    }

    return true;
  }

  @Override
  public Set<String> getVOMembers() {

    long startTime = System.currentTimeMillis();

    Set<String> subjects = new HashSet<>();

    try {

      List<CSVRecord> records = CSVFormat.DEFAULT.parse(new FileReader(mapFile)).getRecords();

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
          logger.debug("Converted subject {} to rfc format {}", subject, rfcSubject);
        }

        subjects.add(rfcSubject);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    long totalTime = System.currentTimeMillis() - startTime;

    logger.debug("Parsing VO {} members from {} took {} msecs.", voName, mapFile, totalTime);

    return subjects;
  }

  @Override
  public String getVOName() {

    return voName;
  }
}
