// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

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
