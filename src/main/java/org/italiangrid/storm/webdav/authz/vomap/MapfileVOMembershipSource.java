package org.italiangrid.storm.webdav.authz.vomap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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

    Assert.hasText(voName);
    Assert.notNull(mapFile);

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
      logger.debug("Invalid CSVRecord: {}. Illegal size: {}", r, r.size());
      return false;
    }

    if (!r.get(0).startsWith("/")) {
      logger.debug("Invalid CSVRecord: {}. Subject does not start with / : {}",
        r, r.get(0));
      return false;
    }

    return true;
  }

  @Override
  public Set<String> getVOMembers() {

    long startTime = System.currentTimeMillis();

    Set<String> subjects = new HashSet<String>();

    CSVParser parser = getParser();

    try {

      List<CSVRecord> records = parser.getRecords();

      for (CSVRecord r : records) {

        if (logger.isDebugEnabled()) {
          logger.debug("Parsed record: {} for VO {}", r, voName);
        }

        if (!isValidCSVRecord(r)) {
          break;
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

    logger.info("Parsing VO {} members from {} took {} msecs.", voName,
      mapFile, totalTime);

    return subjects;
  }

  @Override
  public String getVOName() {

    return voName;
  }

}
