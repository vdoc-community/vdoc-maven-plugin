package com.vdoc.maven.plugin.utils.versions.filter;

import com.vdoc.maven.plugin.utils.project.ProjectContext;
import com.vdoc.maven.plugin.utils.versions.Version;
import com.vdoc.maven.plugin.utils.versions.VersionParser;
import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreaterOrEqualsVersionDirectoryFilter implements FileFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GreaterOrEqualsVersionDirectoryFilter.class);

  private final ProjectContext projectContext;
  private final VersionParser versionParser;

  public GreaterOrEqualsVersionDirectoryFilter(ProjectContext projectContext) {
    this.projectContext = projectContext;
    this.versionParser = new VersionParser();
  }

  @Override
  public boolean accept(File file) {
    if (!file.isDirectory()) {
      return false;
    }

    boolean valid = false;
    try {
      Version version = this.versionParser.parse(file.getName());
      valid = version.compareTo(projectContext.getMinimumRuntimeVersion()) >= 0 ;
    } catch (ParseException e) {
      LOGGER.warn("Not valid version {}", file.getName());
    }
    return valid;

  }
}
