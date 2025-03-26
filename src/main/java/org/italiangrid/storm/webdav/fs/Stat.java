// SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
//
// SPDX-License-Identifier: Apache-2.0

package org.italiangrid.storm.webdav.fs;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

// This is a wrapper of the stat struct needed to obtain the number of 512 B blocks allocated used
// to check is a file is stub
// A runtime check is done in SAConfigurationParser comparing the number obtained from this wrapper
// from the one obtained with the stat command
@FieldOrder({
  "st_dev",
  "st_ino",
  "st_nlink",
  "st_mode",
  "st_uid",
  "st_gid",
  "__pad0",
  "st_rdev",
  "st_size",
  "st_blksize",
  "st_blocks",
  "st_atime",
  "st_mtime",
  "st_ctime",
  "__glibc_reserved"
})
public class Stat extends Structure {
  // ID of device containing file
  public NativeLong st_dev;
  // Inode number
  public NativeLong st_ino;
  // Number of hard links
  public NativeLong st_nlink;
  // File type and mode
  public int st_mode;
  // User ID of owner
  public int st_uid;
  // Group ID of owner
  public int st_gid;
  public int __pad0;
  // Device ID (if special file)
  public NativeLong st_rdev;
  // Total size, in bytes
  public NativeLong st_size;
  // Block size for filesystem I/O
  public NativeLong st_blksize;
  // Number of 512 B blocks allocated
  public NativeLong st_blocks;
  // Time of last access
  public Timespec st_atime;
  // Time of last access
  public Timespec st_mtime;
  // Time of last status change
  public Timespec st_ctime;
  public NativeLong[] __glibc_reserved = new NativeLong[3];
}
