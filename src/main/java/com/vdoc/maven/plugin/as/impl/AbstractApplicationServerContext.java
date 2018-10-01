package com.vdoc.maven.plugin.as.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractApplicationServerContext implements ApplicationServerContext {

  protected final Path home;
  protected final Path ear;
  protected final Path earLib;
  protected final Path war;
  protected final Path custom;

  public AbstractApplicationServerContext(Path home) {
    this.home = home;
    this.ear = this.getEar();
    this.earLib = this.ear.resolve("lib");
    this.war = this.ear.resolve("vdoc.war");
    this.custom = this.war.resolve(Paths.get("WEB-INF", "storage", "custom"));
  }

  @Override
  public Path getEarLib() {
    return earLib;
  }

  @Override
  public Path getWar() {
    return war;
  }

  @Override
  public Path getCustom() {
    return custom;
  }
}
