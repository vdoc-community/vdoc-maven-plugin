package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;

public class JBossApplicationServerContext extends AbstractApplicationServerContext implements ApplicationServerContext {

  public JBossApplicationServerContext(Path home) {
    super(home);
  }
}
