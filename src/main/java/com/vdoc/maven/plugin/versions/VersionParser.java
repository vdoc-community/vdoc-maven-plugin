package com.vdoc.maven.plugin.versions;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionParser {

  private static final Pattern VERSION_REGEX = Pattern.compile("(\\d*)\\.(\\d*)\\.(\\d*)(-(.*))?");
  public static final int MAJOR_INDEX = 1;
  public static final int MINOR_INDEX = 2;
  public static final int FIX_INDEX = 3;
  public static final int FLAG_INDEX = 5;

  public Version parse(String version) throws ParseException {
    Matcher matcher = VERSION_REGEX.matcher(version);
    if(!matcher.matches()) {
      throw new ParseException("Version "+version+" can't be parsed",-1);
    }
    return new VersionBuilder()
        .setMajor(Long.parseLong(matcher.group(MAJOR_INDEX)))
        .setMinor(Long.parseLong(matcher.group(MINOR_INDEX)))
        .setFix(Long.parseLong(matcher.group(FIX_INDEX)))
        .setFlag(matcher.group(FLAG_INDEX))
        .build();
  }

}
