package org.italiangrid.storm.webdav.tape.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

@JsonInclude(Include.NON_NULL)
public class StageStatusRequest {

  String id = UUID.randomUUID().toString();
  Timestamp createdAt = new Timestamp(System.currentTimeMillis());
  List<StageRequestFileStatus> files;
  Timestamp startedAt;
  Timestamp completedAt;

  public StageStatusRequest(List<StageRequestFile> files) {
    this.files = Lists.newArrayList();
    files.forEach(rf -> this.files.add(new StageRequestFileStatus(rf.path)));
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public List<StageRequestFileStatus> getFiles() {
    return files;
  }

  public void setFiles(List<StageRequestFileStatus> files) {
    this.files = files;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Timestamp startedAt) {
    this.startedAt = startedAt;
  }

  public Timestamp getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Timestamp completedAt) {
    this.completedAt = completedAt;
  }

  public void evolve() {

    if (completedAt != null) {
      return;
    }
    files.stream()
        .filter(f -> !f.getStatus().equals(FileStatus.COMPLETED))
        .filter(f -> !f.getStatus().equals(FileStatus.CANCELLED))
        .filter(f -> !f.getStatus().equals(FileStatus.FAILED))
        .forEach(f -> {
          f.evolve();
          if (f.status.equals(FileStatus.STARTED) && startedAt == null) {
            startedAt = f.getStartedAt();
          }
        });
    if (files.stream()
        .filter(f -> !f.getStatus().equals(FileStatus.COMPLETED))
        .filter(f -> !f.getStatus().equals(FileStatus.CANCELLED))
        .filter(f -> !f.getStatus().equals(FileStatus.FAILED))
        .findFirst()
        .isEmpty()) {
      completedAt = new Timestamp(System.currentTimeMillis());
    }
  }
}
