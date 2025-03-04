// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.milton.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ReplaceContentStrategy {

  void replaceContent(InputStream in, Long length, File targetFile) throws IOException;

}
