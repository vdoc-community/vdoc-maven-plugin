package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.utils.as.ApplicationServerContext;
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
public class HardDeployMojo extends AbstractDeployerVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);

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

        ApplicationServerContext applicationServerContext = this.findApplicationServerContext();

        File jar = getJarFile(buildDirectory, jarName, null);
        try {
            File libDirectory = applicationServerContext.getEarLib().toFile();
            // copy jars
            if (jar.exists()) {
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
                File targetCustomFolder = applicationServerContext.getCustom().toFile();
                File targetWebappFolder = applicationServerContext.getWar().toFile();
                for (String sourceRootPath : this.project.getCompileSourceRoots()) {

                    deployToWebapp(targetCustomFolder, targetWebappFolder, sourceRootPath);
                }

                for (String testsSourceRootPath : project.getTestCompileSourceRoots()) {
                    deployToWebapp(targetCustomFolder, targetWebappFolder, testsSourceRootPath);
                }
            }
            if (this.deployDependencies && dependenciesFolder.exists() && dependenciesFolder.isDirectory()) {

                LOGGER.info(String.format("Copy %1$s to %2$s", dependenciesFolder.getAbsolutePath(), libDirectory.getAbsolutePath()));
                FileUtils.copyDirectory(dependenciesFolder, libDirectory);
            }


        } catch (IOException e) {
            throw new MojoFailureException("Deploy fail :", e);
        }

    }
    
    private void deployToWebapp(File targetCustomFolder, File targetWebappFolder, String sourceRootPath) throws IOException {
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
