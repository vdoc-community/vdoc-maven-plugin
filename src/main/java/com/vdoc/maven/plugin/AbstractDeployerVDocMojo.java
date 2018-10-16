package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.as.ApplicationServerContextFactory;
import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.versions.filter.GreaterOrEqualsVersionDirectoryFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by famaridon on 19/05/2014.
 */
public abstract class AbstractDeployerVDocMojo extends AbstractVDocMojo {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeployerVDocMojo.class);
  public static final String VDOC_HOMES_ENV = "VDOC_HOMES";
  private static ApplicationServerContext applicationServerContext = null;
  public static final Object applicationServerContextWriteLock = new Object();

  /**
   * the VDoc home folder if set the apps is copied into apps folder.
   */
  @Parameter(required = false)
  private File vdocHome;

  @Component
  private Prompter prompter;


  public ApplicationServerContext findApplicationServerContext() {

    // double if check singleton to synchronize all on the first assign.
    if (applicationServerContext == null) {
      synchronized (applicationServerContextWriteLock) {
        if (applicationServerContext == null) {
          ProjectContext projectContext = this.findProjectContext();
          applicationServerContext = ApplicationServerContextFactory
              .newInstance(findApplicationServer(projectContext));
          LOGGER.info("Target application server {}", this.applicationServerContext);
        }
      }
    }
    return applicationServerContext;
  }

  private File findApplicationServer(ProjectContext projectContext) {
    File home = null;
    // an home is forced in properties.
    home = findByHomeProperties(projectContext, home);

    // no home search into VDOC_HOMES valid versions
    if (home == null) {
      home = findInVDocHomes(projectContext, home);
    }
    return home;
  }

  private File findInVDocHomes(ProjectContext projectContext, File home) {
    LOGGER.info("vdocHome property is not set use " + VDOC_HOMES_ENV + " environment variable");
    String vdocHomesPath = System.getenv(VDOC_HOMES_ENV);
    if (StringUtils.isNotBlank(vdocHomesPath)) {
      File vdocHomes = new File(vdocHomesPath);
      if (!vdocHomes.exists()) {
        LOGGER.warn("'{}' doesn't exist", vdocHomesPath);
      } else if (!vdocHomes.isDirectory()) {
        LOGGER.warn("'{}' is not a directory", vdocHomesPath);
      } else {
        // search for valid versions
        File[] validVersions = vdocHomes
            .listFiles(new GreaterOrEqualsVersionDirectoryFilter(projectContext));

        if (validVersions.length == 0) {
          throw new IllegalStateException("No valid home found!");
        } else if (validVersions.length == 1) {
          home = validVersions[0];
        } else {
          home = selectVersion(validVersions);
        }
      }
    }
    return home;
  }

  private File selectVersion(File[] validVersions) {
    File home;
    List<String> options = new ArrayList<>(validVersions.length);
    StringBuilder message = new StringBuilder("Choose the target version :  \n");

    for (int i = 0; i < validVersions.length; i++) {
      message.append(String.format("\t %1s. %2s \n", i, validVersions[i].getName()));
      options.add(Integer.toString(i));
    }
    message.append("options :");

    int selectedVersion = 0;
    try {
      selectedVersion = Integer.parseInt(prompter.prompt(message.toString(), options, "0"));
    } catch (PrompterException e) {
      throw new IllegalStateException(e);
    }

    home = validVersions[selectedVersion];
    return home;
  }

  private File findByHomeProperties(ProjectContext projectContext, File home) {
    if (this.vdocHome != null) {
      LOGGER.info("vdocHome property is set to '{}' try to use it.", this.vdocHome);
      if (!this.vdocHome.exists()) {
        LOGGER.warn("'{}' doesn't exist", this.vdocHome);
      } else if (!this.vdocHome.isDirectory()) {
        LOGGER.warn("'{}' is not a directory", this.vdocHome);
      } else {
        home = this.vdocHome;
      }
    }
    return home;
  }

  /**
   * set {@link AbstractDeployerVDocMojo#vdocHome} property
   *
   * @param vdocHome set the vdocHome property
   **/
  public void setVdocHome(File vdocHome) {
    this.vdocHome = vdocHome;
  }
}
