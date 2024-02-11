package org.italiangrid.storm.webdav.fs.attrs;

public enum ExtendedAttributes {

  STORM_ADLER32_CHECKSUM_ATTR_NAME("storm.checksum.adler32"),
  STORM_PREMIGRATE_ATTR_NAME("storm.premigrate"),
  STORM_MIGRATED_ATTR_NAME("storm.migrated"),
  STORM_RECALL_IN_PROGRESS_ATTR_NAME("storm.TSMRecT");

  private final String attrName;

  private ExtendedAttributes(String attrName) {
    this.attrName = attrName;
  }

  @Override
  public String toString() {
    return attrName;
  }
}
