package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;

public abstract class AbstractApplicationServerContext implements ApplicationServerContext {
  protected final Path home;

  public AbstractApplicationServerContext(Path home) {
    this.home = home;
  }
}
