package com.vdoc.maven.plugin.deploy.vdoc.pom;

import com.vdoc.maven.plugin.deploy.vdoc.beans.Artifact;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Maven;
import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Set;

/**
 * Created by famaridon on 15/02/15.
 */
public class ParentPOMGeneratorImpl implements ParentPOMGenerator {

    public static final Version fmVersion = Configuration.VERSION_2_3_20;

    private final Configuration configuration;
    private final ParentPOM pom;
    private final Maven maven;
    private final String targetVersion;
    private final Set<Artifact> artifactList;

    public ParentPOMGeneratorImpl(Maven maven, ParentPOM pom, Set<Artifact> artifactList, String targetVersion) throws IOException {
        super();
        this.maven = maven;
        this.pom = pom;
        this.artifactList = artifactList;
        this.targetVersion = targetVersion;

        // build the full pom
        this.configuration = new Configuration(ParentPOMGeneratorImpl.fmVersion);

        // Specify the data source where the template files come from.
        this.configuration.setDirectoryForTemplateLoading(this.maven.getMavenHome());

        // Specify how templates will see the data-model. This is an advanced topic...
        // for now just use this:

        // Create the builder:
        BeansWrapperBuilder builder = new BeansWrapperBuilder(ParentPOMGeneratorImpl.fmVersion);
        // Set desired BeansWrapper configuration properties:
        builder.setUseModelCache(true);
        builder.setExposeFields(true);
        this.configuration.setObjectWrapper(builder.build());

        // Set your preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        this.configuration.setDefaultEncoding("UTF-8");

        // Sets how errors will appear. Here we assume we are developing HTML pages.
        // For production systems TemplateExceptionHandler.RETHROW_HANDLER is better.
        this.configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // At least in new projects, specify that you want the fixes that aren't
        // 100% backward compatible too (these are very low-risk changes as far as the
        // 1st and 2nd version number remains):
        this.configuration.setIncompatibleImprovements(ParentPOMGeneratorImpl.fmVersion);

    }

    @Override
    public File generate() throws PomGenerationException {
        try {
            this.copyTemplateLocaly();
            Template temp = this.configuration.getTemplate(this.pom.getFtlName());

            File pomFile = new File(this.maven.getMavenHome(), "pom.xml");
            try (Writer out = new FileWriter(pomFile)) {
                temp.process(this, out);
                out.flush();
            }
            return pomFile;
        } catch (IOException | TemplateException e) {
            throw new PomGenerationException(e);
        }
    }

    public void copyTemplateLocaly() throws IOException {
        try (
                InputStream input = this.getClass().getClassLoader().getResourceAsStream("pom/" + this.pom.getFtlName());
                FileOutputStream outputStream = new FileOutputStream(new File(this.maven.getMavenHome(), this.pom.getFtlName()))
        ) {
            IOUtils.copy(input, outputStream);
            outputStream.flush();
        }
    }

    public Maven getMaven() {
        return this.maven;
    }

    public String getTargetVersion() {
        return this.targetVersion;
    }

    public Set<Artifact> getArtifactList() {
        return this.artifactList;
    }
}
