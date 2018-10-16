package com.vdoc.maven.plugin.versions;

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
  void compareGreaterMajor() throws ParseException {
    Version big = this.parser.parse("16.0.0");
    Version small = this.parser.parse("15.0.0");
    Assertions.assertFalse(small.isGreater(big));
    Assertions.assertTrue(big.isGreater(small));
  }

  @Test
  void compareGreaterMinor() throws ParseException {
    Version big = this.parser.parse("15.3.0");
    Version small = this.parser.parse("15.0.0");
    Assertions.assertFalse(small.isGreater(big));
    Assertions.assertTrue(big.isGreater(small));
  }

  @Test
  void compareGreaterFix() throws ParseException {
    Version big = this.parser.parse("15.0.8");
    Version small = this.parser.parse("15.0.0");
    Assertions.assertFalse(small.isGreater(big));
    Assertions.assertTrue(big.isGreater(small));
  }

  @Test
  void compareGreaterFlag() throws ParseException {
    Version big = this.parser.parse("15.0.0");
    Version small = this.parser.parse("15.0.0-SNAPSHOT");
    Assertions.assertFalse(small.isGreater(big));
    Assertions.assertTrue(big.isGreater(small));
  }


  @Test
  void compareLessMajor() throws ParseException {
    Version big = this.parser.parse("16.0.0");
    Version small = this.parser.parse("15.0.0");
    Assertions.assertTrue(small.isLess(big));
    Assertions.assertFalse(big.isLess(small));
  }

  @Test
  void compareLessMinor() throws ParseException {
    Version big = this.parser.parse("15.10.0");
    Version small = this.parser.parse("15.0.0");
    Assertions.assertTrue(small.isLess(big));
    Assertions.assertFalse(big.isLess(small));
  }

  @Test
  void compareLessFix() throws ParseException {
    Version big = this.parser.parse("15.0.8");
    Version small = this.parser.parse("15.0.5");
    Assertions.assertTrue(small.isLess(big));
    Assertions.assertFalse(big.isLess(small));
  }

  @Test
  void compareLessFlag() throws ParseException {
    Version big = this.parser.parse("15.0.0");
    Version small = this.parser.parse("15.0.0-SNAPSHOT");
    Assertions.assertTrue(small.isLess(big));
    Assertions.assertFalse(big.isLess(small));
  }

  @Test
  void compareGreaterOrEqualsMinor() throws ParseException {
    Version big = this.parser.parse("15.4.0-SNAPSHOT");
    Version small = this.parser.parse("15.3.5-SNAPSHOT");
    Assertions.assertFalse(small.compareTo(big) >= 0);
    Assertions.assertTrue(big.compareTo(small) >= 0);
  }

  @Test
  void isEquals() throws ParseException {
    Version first = this.parser.parse("15.4.8");
    Version second = this.parser.parse("15.4.8");
    Assertions.assertTrue(first.equals(second));
  }

  @Test
  void isEqualsFlags() throws ParseException {
    Version first = this.parser.parse("15.4.8-SNAPSHOT");
    Version second = this.parser.parse("15.4.8-SNAPSHOT");
    Assertions.assertTrue(first.equals(second));
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