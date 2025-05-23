// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.tpc.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.MDC;
import org.springframework.util.Assert;

public class ClientInfo {

  private static final String INVALID_CLIENTINFO_HEADER_MESSAGE = "Invalid ClientInfo header: %s";

  public static final String CLIENT_INFO_MDC_KEY = "tpc.clientInfo";

  public static final String JOB_ID_KEY = "job-id";
  public static final String FILE_ID_KEY = "file-id";
  public static final String RETRY_COUNT_KEY = "retry";

  final String jobId;
  final String fileId;
  final int retryCount;

  private ClientInfo(String jobId, String fileId, int retryCount) {
    this.jobId = jobId;
    this.fileId = fileId;
    this.retryCount = retryCount;
  }

  public String getJobId() {
    return jobId;
  }

  public String getFileId() {
    return fileId;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public static ClientInfo fromHeaderString(String headerString) {
    Assert.hasText(headerString, String.format(INVALID_CLIENTINFO_HEADER_MESSAGE, headerString));
    Map<String, String> splitResult =
        Arrays.stream(headerString.split(";"))
            .map(entry -> entry.split("="))
            .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim()));
    Assert.isTrue(
        splitResult.containsKey(JOB_ID_KEY),
        String.format(INVALID_CLIENTINFO_HEADER_MESSAGE, headerString));
    Assert.isTrue(
        splitResult.containsKey(FILE_ID_KEY),
        String.format(INVALID_CLIENTINFO_HEADER_MESSAGE, headerString));
    Assert.isTrue(
        splitResult.containsKey(RETRY_COUNT_KEY),
        String.format(INVALID_CLIENTINFO_HEADER_MESSAGE, headerString));
    return new ClientInfo(
        splitResult.get(JOB_ID_KEY),
        splitResult.get(FILE_ID_KEY),
        Integer.parseInt(splitResult.get(RETRY_COUNT_KEY)));
  }

  public void addToMDC() {
    final String ciStr = String.format("job-id:%s,file-id:%s,retry:%d", jobId, fileId, retryCount);
    MDC.put(CLIENT_INFO_MDC_KEY, ciStr);
  }
}
