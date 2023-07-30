package org.italiangrid.storm.webdav.tape.model;

import java.util.List;

import javax.validation.constraints.NotEmpty;

public class StageRequest {

  @NotEmpty(message = "Input file list cannot be empty.")
  List<StageRequestFile> files;

  public List<StageRequestFile> getFiles() {
    return files;
  }

  public void setFiles(List<StageRequestFile> files) {
    this.files = files;
  }
  
}
