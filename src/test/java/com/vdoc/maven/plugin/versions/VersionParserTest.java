package com.vdoc.maven.plugin.versions;

import com.sun.org.apache.regexp.internal.RE;
import java.text.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class VersionParserTest {

  private static final String SNAPSHOT_FLAG = "SNAPSHOT";
  private static final String RC_FLAG = "RC";
  private static final long MAJOR = 16;
  private static final long MINOR = 0;
  private static final long FIX = 0;
  private static final String RELEASE_VERSION = MAJOR + "." + MINOR + "." + FIX;
  private static final String SNAPSHOT_VERSION = RELEASE_VERSION + "-" + SNAPSHOT_FLAG;
  private static final String RC_VERSION = RELEASE_VERSION + "-" + RC_FLAG;
  private VersionParser parser;

  @BeforeAll
  public void beforeEach() {
    this.parser = new VersionParser();
  }

  @Test
  void parseRelease() throws ParseException {
    checkVersion(RELEASE_VERSION, null);
  }

  @Test
  void parseSnapshot() throws ParseException {
    checkVersion(SNAPSHOT_VERSION, SNAPSHOT_FLAG);
  }

  @Test
  void parseReleaseCandidate() throws ParseException {
    checkVersion(RC_VERSION, RC_FLAG);
  }

  @Test
  void notParseableVersion() {
    testNotParsable("16.0-SNAPSHOT");
    testNotParsable("16-SNAPSHOT");
    testNotParsable("SNAPSHOT");
    testNotParsable("A.B.C");
    testNotParsable("A");
  }

  private void testNotParsable(String notParsableVersion) {
    try {
      checkVersion(notParsableVersion, null);
      Assertions.fail();
    } catch (ParseException e) {
      // ok wee get exception
    }
  }

  private void checkVersion(String version, String flag) throws ParseException {
    Version v = parser.parse(version);
    Assertions.assertEquals(MAJOR,v.getMajor());
    Assertions.assertEquals(MINOR,v.getMinor());
    Assertions.assertEquals(FIX,v.getFix());
    Assertions.assertEquals(flag,v.getFlag());
    Assertions.assertEquals(version,v.toString());
  }
}