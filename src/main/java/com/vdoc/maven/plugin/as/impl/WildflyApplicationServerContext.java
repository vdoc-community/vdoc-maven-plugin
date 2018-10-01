package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;

public class WildflyApplicationServerContext extends AbstractApplicationServerContext implements ApplicationServerContext {

  public WildflyApplicationServerContext(Path home) {
    super(home);
  }
}
