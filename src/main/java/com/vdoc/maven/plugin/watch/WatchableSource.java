package com.vdoc.maven.plugin.watch;

import com.vdoc.maven.plugin.utils.as.ApplicationServerContext;
import java.nio.file.Path;
import java.util.Set;

public interface WatchableSource {
  Path getSource();
  Set<String> getExcludeMatchers();
  Path getTo(Path relativePath, ApplicationServerContext applicationServerContext);
}
