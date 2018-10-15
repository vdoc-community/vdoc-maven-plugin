package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.watch.WatchableSource;
import java.nio.file.Path;
import java.util.Set;

public class WebappWatchableSource extends AbstractWatchableSource {

  public WebappWatchableSource(Path source) {
    super(source);
  }

  @Override
  public Path getTo(Path relativePath, ApplicationServerContext applicationServerContext) {
    return applicationServerContext.getWar().resolve(relativePath);
  }
}
