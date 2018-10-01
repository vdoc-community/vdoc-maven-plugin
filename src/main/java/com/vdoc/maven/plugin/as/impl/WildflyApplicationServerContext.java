package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WildflyApplicationServerContext extends AbstractApplicationServerContext implements
    ApplicationServerContext {

  public WildflyApplicationServerContext(Path home) {
    super(home);
  }

  @Override
  public Path getEar() {
    return this.home.resolve(Paths.get("wildfly", "standalone", "deployments", "vdoc.ear"));
  }
}
