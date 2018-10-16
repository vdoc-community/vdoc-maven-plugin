package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.utils.as.ApplicationServerContext;
import java.nio.file.Path;

public class WebappWatchableSource extends AbstractWatchableSource {

  public WebappWatchableSource(Path source) {
    super(source);
  }

  @Override
  public Path getTo(Path relativePath, ApplicationServerContext applicationServerContext) {
    return applicationServerContext.getWar().resolve(relativePath);
  }
}
