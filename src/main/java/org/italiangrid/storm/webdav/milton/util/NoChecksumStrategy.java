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
package org.italiangrid.storm.webdav.milton.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.italiangrid.storm.webdav.error.StoRMWebDAVError;
import org.italiangrid.storm.webdav.utils.RangeCopyHelper;

public class NoChecksumStrategy implements ReplaceContentStrategy {


  @Override
  public void replaceContent(InputStream in, Long length, File targetFile) throws IOException {
    
    if (RangeCopyHelper.rangeCopy(in, targetFile, 0, length) != length) {
      throw new StoRMWebDAVError("Incomplete copy error!");
    }
  }

}
