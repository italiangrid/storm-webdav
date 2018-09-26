package org.italiangrid.storm.webdav.tpc.transfer;

import java.net.URI;

import com.google.common.collect.Multimap;

public interface TransferRequest {
  
  String path();
  
  URI remoteURI();
  
  Multimap<String, String> transferHeaders();
  
  boolean verifyChecksum();
  
  boolean overwrite();
  
}
