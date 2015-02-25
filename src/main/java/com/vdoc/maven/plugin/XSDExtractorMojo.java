package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.xsd.XSDFinder;
import com.vdoc.maven.plugin.xsd.XSDFinderImpl;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * this task can deploy all VDoc jars into a repository
 */
@Mojo(name = "xsd-extract", threadSafe = false, requiresProject = false, requiresDirectInvocation = true)
public class XSDExtractorMojo extends AbstractVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSDExtractorMojo.class);

    /**
     * the VDoc home folder
     */
    @Parameter(property = "vdocHome", required = true)
    protected File vdocHome;
    /**
     * the xsd output directory
     */
    @Parameter(defaultValue = "${vdocHome}/xsd/")
    protected File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (this.outputDirectory.exists() && this.outputDirectory.isFile()) {
            throw new MojoExecutionException("Output directory should not be a folder!");
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }

        // create the file filter to include all jar whose start with VDoc or VDP.
        OrFileFilter prefixFileFilter = new OrFileFilter();
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDoc"));
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDP"));

        AndFileFilter fileFilter = new AndFileFilter();
        fileFilter.addFileFilter(prefixFileFilter);
        fileFilter.addFileFilter(new SuffixFileFilter(".jar"));

        LOGGER.info("Start scanning the vdoc from :" + this.vdocHome.getAbsolutePath());
        Set<File> fileSet = new HashSet<>();
        LOGGER.debug("Start reading vdoc.ear folder");
        File vdocEar = new File(this.vdocHome, "/configurator/vdoc.ear/");
        fileSet.addAll(Arrays.asList(vdocEar.listFiles((FileFilter) fileFilter)));

        LOGGER.debug("Start reading vdoc.ear/lib folder");
        File vdocEarLib = new File(vdocEar, "lib");
        fileSet.addAll(Arrays.asList(vdocEarLib.listFiles((FileFilter) fileFilter)));

        Set<File> artifacts = this.extractXSD(fileSet);


    }

    private Set<File> extractXSD(Set<File> fileSet) throws MojoFailureException {
        Validate.notEmpty(fileSet);

        LOGGER.info("Prepare all file for XSD extraction");
        Set<XSDFinder> xsdFinderSet = new HashSet<>(fileSet.size());
        try {
            for (File jar : fileSet) {
                XSDFinder xsdFinder = new XSDFinderImpl(jar, this.outputDirectory);
                xsdFinderSet.add(xsdFinder);
            }

            Set<File> xsdSet = new HashSet<>(fileSet.size());

            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            // use ExecutorCompletionService to take result when it's possible
            CompletionService<List<File>> completionService = new ExecutorCompletionService<>(pool);
            // join all deployment.
            try {
                for (XSDFinder xsdFinder : xsdFinderSet) {
                    completionService.submit(xsdFinder);
                }

                for (int i = 0; i < xsdFinderSet.size(); i++) {
                    Future<List<File>> future = completionService.take();
                    List<File> finished = future.get();
                    xsdSet.addAll(finished);
                    for (File xsd : finished) {
                        LOGGER.info("XSD file extracted {}.", xsd);
                    }
                }
                return xsdSet;
            } catch (InterruptedException | ExecutionException e) {
                throw new MojoFailureException("an artifact deploy have fail!");
            } finally {
                pool.shutdown();
            }
        } catch (IOException e) {
            throw new MojoFailureException("can't read some files", e);
        } finally {
            for (XSDFinder xsdFinder : xsdFinderSet) {
                try {
                    xsdFinder.close();
                } catch (IOException e) {
                    LOGGER.error("stream can't be closed.", e);
                }
            }
        }
    }

}
