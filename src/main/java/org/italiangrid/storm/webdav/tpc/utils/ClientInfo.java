/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2020.
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
package org.italiangrid.storm.webdav.tpc.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Map;

import org.apache.log4j.MDC;

import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;

public class ClientInfo {

  public static final String CLIENT_INFO_MDC_KEY = "tpc.clientInfo";
  
  public static final String JOB_ID_KEY = "job-id";
  public static final String FILE_ID_KEY = "file-id";
  public static final String RETRY_COUNT_KEY = "retry";

  private static final MapSplitter SPLITTER =
      Splitter.on(";").trimResults().withKeyValueSeparator('=');

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
    checkArgument(!isNullOrEmpty(headerString), "Invalid ClientInfo header: %s", headerString);
    Map<String, String> splitResult = SPLITTER.split(headerString);
    checkArgument(splitResult.containsKey(JOB_ID_KEY), "Invalid ClientInfo header: %s",
        headerString);
    checkArgument(splitResult.containsKey(FILE_ID_KEY), "Invalid ClientInfo header: %s",
        headerString);
    checkArgument(splitResult.containsKey(RETRY_COUNT_KEY), "Invalid ClientInfo header: %s",
        headerString);
    return new ClientInfo(splitResult.get(JOB_ID_KEY), splitResult.get(FILE_ID_KEY),
        Integer.parseInt(splitResult.get(RETRY_COUNT_KEY)));
  }

  public void addToMDC() {
    final String ciStr = String.format("job-id:%s,file-id:%s,retry:%d", jobId, fileId, retryCount);
    MDC.put(CLIENT_INFO_MDC_KEY, ciStr);
    
  }
}
