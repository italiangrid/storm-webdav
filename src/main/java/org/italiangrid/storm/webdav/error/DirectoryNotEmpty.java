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
package org.italiangrid.storm.webdav.error;

import org.italiangrid.storm.webdav.milton.StoRMDirectoryResource;

public class DirectoryNotEmpty extends StoRMWebDAVError {


  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DirectoryNotEmpty(StoRMDirectoryResource r) {
    super(String.format("Directory is not empty: %s", r.getName()));
  }

}
