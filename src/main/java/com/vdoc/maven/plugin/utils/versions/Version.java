package com.vdoc.maven.plugin.utils.versions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Version implements Comparable<Version>{

  private final long major;
  private final long minor;
  private final long fix;
  private final String flag;

  public Version(long major, long minor, long fix, String flag) {
    this.major = major;
    this.minor = minor;
    this.fix = fix;
    this.flag = flag;
  }

  public long getMajor() {
    return major;
  }

  public long getMinor() {
    return minor;
  }

  public long getFix() {
    return fix;
  }

  public String getFlag() {
    return flag;
  }

  public boolean isGreater(Version version) {
    return this.compareTo(version) > 0;
  }

  public boolean haveFlag() {
    return this.flag == null;
  }

  public boolean isLess(Version version) {
    return !(this.isGreater(version) || this.equals(version));
  }

  @Override
  public int compareTo(Version other) {
    long result = major - other.major;
    if (result == 0) {
      result = minor - other.minor;
      if (result == 0) {
        result = fix - other.fix;
        if (result == 0) {
          if(flag == null && other.flag != null) {
            result = 1;
          } else if(flag != null && other.flag == null) {
            result = -1;
          } else {
            result = flag.compareTo(other.flag);
          }
        }
      }
    }
    return (int) result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Version version = (Version) o;

    return new EqualsBuilder()
        .append(major, version.major)
        .append(minor, version.minor)
        .append(fix, version.fix)
        .append(flag, version.flag)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(major)
        .append(minor)
        .append(fix)
        .append(flag)
        .toHashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.major).append('.');
    builder.append(this.minor).append('.');
    builder.append(this.fix);

    if(this.flag != null) {
      builder.append('-').append(this.flag);
    }

    return builder.toString();
  }

}
