package com.vdoc.maven.plugin.project;

import com.vdoc.maven.plugin.versions.Version;
import com.vdoc.maven.plugin.watch.WatchableSource;
import org.apache.maven.project.MavenProject;

public interface ProjectContext {

  MavenProject getMavenProject();
  Version getMinimumRuntimeVersion();
  Iterable<WatchableSource> getWatchableSources();


}
