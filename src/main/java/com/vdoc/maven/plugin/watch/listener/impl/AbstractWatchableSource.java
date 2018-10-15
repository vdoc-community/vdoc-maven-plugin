package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.watch.WatchableSource;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractWatchableSource implements WatchableSource {
  private final Path source;
  protected final Set<String> excludeMatchers;

  public AbstractWatchableSource(Path source) {
    this.source = source;
    this.excludeMatchers = new HashSet<>();
  }

  public Path getSource(){
    return this.source;
  }

  public Set<String> getExcludeMatchers(){
    return this.excludeMatchers;
  }
}
