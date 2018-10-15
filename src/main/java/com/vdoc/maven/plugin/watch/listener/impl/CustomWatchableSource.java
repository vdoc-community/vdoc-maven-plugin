package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import java.nio.file.Path;

public class CustomWatchableSource extends AbstractWatchableSource {

  public CustomWatchableSource(Path source) {
    super(source);
    this.excludeMatchers.add("webapp*");
  }

  @Override
  public Path getTo(Path relativePath, ApplicationServerContext applicationServerContext) {
    return applicationServerContext.getCustom().resolve(relativePath);
  }
}
