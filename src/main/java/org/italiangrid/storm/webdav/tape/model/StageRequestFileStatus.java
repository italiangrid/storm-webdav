package org.italiangrid.storm.webdav.tape.model;

import java.sql.Timestamp;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class StageRequestFileStatus {

  private static Random rd = new Random();


  String path;
  boolean onDisk;
  FileStatus status;
  Timestamp startedAt;
  Timestamp finishedAt;
  String error;

  public StageRequestFileStatus(String path) {
    this.path = path;
    this.onDisk = false;
    this.status = FileStatus.SUBMITTED;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isOnDisk() {
    return onDisk;
  }

  public void setOnDisk(boolean onDisk) {
    this.onDisk = onDisk;
  }

  public FileStatus getStatus() {
    return status;
  }

  public void setStatus(FileStatus status) {
    this.status = status;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Timestamp startedAt) {
    this.startedAt = startedAt;
  }

  public Timestamp getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(Timestamp finishedAt) {
    this.finishedAt = finishedAt;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean getRandomBoolean(double p) {
    return rd.nextFloat() < p;
  }

  public void evolve() {

    if (FileStatus.COMPLETED.equals(status)
        || FileStatus.CANCELLED.equals(status)
        || FileStatus.FAILED.equals(status)) {
      return;
    }
    if (FileStatus.SUBMITTED.equals(status)) {
      status = FileStatus.STARTED;
      startedAt = new Timestamp(System.currentTimeMillis());
      return;
    }
    if (FileStatus.STARTED.equals(status)) {
      boolean isRunning = getRandomBoolean(0.3);
      if (isRunning) {
        return;
      }
      boolean isCompleted = getRandomBoolean(0.5);
      if (isCompleted) {
        status = FileStatus.COMPLETED;
        finishedAt = new Timestamp(System.currentTimeMillis());
        onDisk = true;
        return;
      }
      boolean isCanceled = getRandomBoolean(0.1);
      if (isCanceled) {
        status = FileStatus.CANCELLED;
        finishedAt = new Timestamp(System.currentTimeMillis());
        return;
      }
      boolean isFailed = getRandomBoolean(0.2);
      if (isFailed) {
        status = FileStatus.FAILED;
        finishedAt = new Timestamp(System.currentTimeMillis());
        error = "Recall failed!";
        return;
      }
    }
  }
}
