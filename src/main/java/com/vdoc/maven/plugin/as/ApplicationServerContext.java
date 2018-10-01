package com.vdoc.maven.plugin.as;

import java.nio.file.Path;

public interface ApplicationServerContext {

  public Path getEar();
  public Path getEarLib();
  public Path getWar();
  public Path getCustom();
}
