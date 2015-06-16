package com.vdoc.maven.plugin;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * this task is used to deploy a project to the target VDoc install.
 */
@Mojo(name = "hard-deploy", threadSafe = true, defaultPhase = LifecyclePhase.PACKAGE)
public class HardDeployMojo extends AbstractVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);

    protected File vdocEAR;

    /**
     * the VDoc home folder.
     */
    @Parameter(required = true)
    protected File vdocHome;
    /**
     * custom folder must be updated
     */
    @Parameter(required = true, defaultValue = "true")
    protected boolean deployDependencies;
    /**
     * custom folder must be updated
     */
    @Parameter(required = true, defaultValue = "true")
    protected boolean withCustom;
    /**
     * test jar must be deployed
     */
    @Parameter(required = false, defaultValue = "true")
    protected boolean includeTest;
    /**
     * source jar must be deployed
     */
    @Parameter(required = false, defaultValue = "false")
    protected boolean includeSource;
    /**
     * where dependencies jar can't be found
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}/lib")
    private File dependenciesFolder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        this.getProject().getFile();
        if ((this.vdocHome == null) || !this.vdocHome.exists() || this.getProject().getFile().equals(vdocHome)) {
            LOGGER.error("VDoc home not found or invalid path.");
            return;
        }

        this.vdocEAR = new File(this.vdocHome, "/JBoss/server/all/deploy/vdoc.ear/");
        File jar = getJarFile(buildDirectory, jarName, null);
        try {
            // copy jars
            if (jar.exists()) {

                File libDirectory = new File(vdocEAR, "lib");
                LOGGER.info(String.format("Copy %1$s to %2$s", jar.getAbsolutePath(), libDirectory.getAbsolutePath()));
                FileUtils.copyFileToDirectory(jar, libDirectory);

                if (this.includeTest) {

                    File testJar = getJarFile(buildDirectory, jarName, "tests");
                    if (testJar.exists()) {
                        LOGGER.info(String.format("Copy test jar %1$s to %2$s", testJar.getAbsolutePath(), libDirectory.getAbsolutePath()));
                        FileUtils.copyFileToDirectory(testJar, libDirectory);
                    } else {
                        LOGGER.warn("No test jar found!");
                    }
                }

                if (this.includeSource) {

                    File sourceJar = getJarFile(buildDirectory, jarName, "source");
                    if (sourceJar.exists()) {
                        LOGGER.info(String.format("Copy source jar %1$s to %2$s", sourceJar.getAbsolutePath(), libDirectory.getAbsolutePath()));
                        FileUtils.copyFileToDirectory(sourceJar, libDirectory);
                    } else {
                        LOGGER.warn("No source jar found!");
                    }

                }
            }

            if (this.withCustom) {
                // copy custom
                File targetCustomFolder = new File(vdocEAR, "vdoc.war/WEB-INF/storage/custom/");
                File targetWebappFolder = new File(vdocEAR, "vdoc.war/");
                for (String sourceRootPath : this.project.getCompileSourceRoots()) {

                    File sourceRoot = new File(sourceRootPath);
                    File customFolder = new File(sourceRoot.getParentFile(), "custom");

                    if (customFolder.exists()) {
                        LOGGER.info(String.format("Copy custom %1$s to %2$s", customFolder.getAbsolutePath(), targetCustomFolder.getAbsolutePath()));
                        FileUtils.copyDirectory(customFolder, targetCustomFolder, notWebAppFolderFileFilter);
                        File customWebappFolder = new File(customFolder, "webapp");
                        if (customWebappFolder.exists()) {
                            LOGGER.info(String.format("Copy webapp %1$s to %2$s", customWebappFolder.getAbsolutePath(), targetWebappFolder.getAbsolutePath()));
                            FileUtils.copyDirectory(customWebappFolder, targetWebappFolder);
                        }
                    }
                }
            }
            if (this.deployDependencies && dependenciesFolder.exists() && dependenciesFolder.isDirectory()) {
                File vdocEARLib = new File(this.vdocEAR, "lib");

                LOGGER.info(String.format("Copy %1$s to %2$s", dependenciesFolder.getAbsolutePath(), vdocEARLib.getAbsolutePath()));
                FileUtils.copyDirectory(dependenciesFolder, vdocEARLib);
            }


        } catch (IOException e) {
            throw new MojoFailureException("Deploy fail :", e);
        }

    }

}
