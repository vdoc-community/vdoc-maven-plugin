package com.vdoc.maven.plugin.versions;

public class VersionBuilder {

  private long major;
  private long minor;
  private long fix;
  private String flag;

  public VersionBuilder setMajor(long major) {
    this.major = major;
    return this;
  }

  public VersionBuilder setMinor(long minor) {
    this.minor = minor;
    return this;
  }

  public VersionBuilder setFix(long fix) {
    this.fix = fix;
    return this;
  }

  public VersionBuilder setFlag(String flag) {
    this.flag = flag;
    return this;
  }

  public Version build() {
    return new Version(major, minor, fix, flag);
  }
}