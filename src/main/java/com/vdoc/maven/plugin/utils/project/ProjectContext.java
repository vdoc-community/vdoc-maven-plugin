package com.vdoc.maven.plugin.utils.project;

import com.vdoc.maven.plugin.utils.versions.Version;
import com.vdoc.maven.plugin.watch.WatchableSource;
import org.apache.maven.project.MavenProject;

public interface ProjectContext {

  MavenProject getMavenProject();
  Version getMinimumRuntimeVersion();
  Iterable<WatchableSource> getWatchableSources();


}
