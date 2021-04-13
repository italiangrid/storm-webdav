/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2021.
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
package org.italiangrid.storm.webdav.server.servlet.resource;

import java.util.Date;

public class StormFsResourceView {

  final String name;

  final boolean isDirectory;

  final String path;

  final long sizeInBytes;

  final Date lastModificationTime;

  final Date creationTime;

  private StormFsResourceView(Builder b) {
    if (b.isDirectory && !b.name.endsWith("/")) {
      this.name = b.name +"/";
    } else {
      this.name = b.name;
    }
    
    this.isDirectory = b.isDirectory;
    this.path = b.path;
    this.sizeInBytes = b.sizeInBytes;
    this.lastModificationTime = b.lastModificationTime;
    this.creationTime = b.creationTime;
    
    
  }

  public String getName() {
    return name;
  }


  public boolean isDirectory() {
    return isDirectory;
  }


  public String getPath() {
    return path;
  }


  public long getSizeInBytes() {
    return sizeInBytes;
  }


  public Date getLastModificationTime() {
    return lastModificationTime;
  }


  public Date getCreationTime() {
    return creationTime;
  }


  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    String name;
    boolean isDirectory;
    String path;
    long sizeInBytes;
    Date lastModificationTime;
    Date creationTime;

    public Builder() {}

    public Builder withName(String name) {
      this.name = name;
      return this;
    }


    public Builder withIsDirectory(boolean isDirectory) {
      this.isDirectory = isDirectory;
      return this;
    }

    public Builder withPath(String path) {
      this.path = path;
      return this;
    }

    public Builder withSizeInBytes(long syzeInBytes) {
      this.sizeInBytes = syzeInBytes;
      return this;
    }

    public Builder withLastModificationTime(Date lastModificationTime) {
      this.lastModificationTime = lastModificationTime;
      return this;
    }

    public Builder withCreationTime(Date creationTime) {
      this.creationTime = creationTime;
      return this;
    }

    public StormFsResourceView build() {
      return new StormFsResourceView(this);
    }


  }

}
