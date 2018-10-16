package com.vdoc.maven.plugin.utils.as;

import java.nio.file.Path;

public interface ApplicationServerContext {

  public Path getEar();
  public Path getEarLib();
  public Path getWar();
  public Path getApps();
  public Path getCustom();
}
