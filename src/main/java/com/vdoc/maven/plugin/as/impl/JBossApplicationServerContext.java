package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JBossApplicationServerContext extends AbstractApplicationServerContext implements
    ApplicationServerContext {

  public JBossApplicationServerContext(Path home) {
    super(home);
  }

  @Override
  public Path getEar() {
    return this.home.resolve(Paths.get("JBoss", "server", "all", "deploy", "vdoc.ear"));
  }
}
