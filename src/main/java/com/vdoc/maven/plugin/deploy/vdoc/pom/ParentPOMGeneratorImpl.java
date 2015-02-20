package com.vdoc.maven.plugin.deploy.vdoc.pom;

import com.vdoc.maven.plugin.DeployVDocMojo;
import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by famaridon on 15/02/15.
 */
public class ParentPOMGeneratorImpl implements ParentPOMGenerator {

    public static final Version fmVersion = Configuration.VERSION_2_3_20;

    private final Configuration configuration;
    private final File workingDirectory;
    private final ParentPOM pom;
    /**
     * TODO : use interface or custom bean.
     */
    private final DeployVDocMojo mojo;

    public ParentPOMGeneratorImpl(File workingDirectory, ParentPOM pom, DeployVDocMojo mojo) throws IOException {
        super();
        this.workingDirectory = workingDirectory;
        this.pom = pom;
        this.mojo = mojo;

        // build the full pom
        this.configuration = new Configuration(ParentPOMGeneratorImpl.fmVersion);

        // Specify the data source where the template files come from.
        this.configuration.setDirectoryForTemplateLoading(this.workingDirectory);

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

            File pomFile = new File(this.workingDirectory, "pom.xml");
            try (Writer out = new FileWriter(pomFile)) {
                temp.process(this.mojo, out);
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
                FileOutputStream outputStream = new FileOutputStream(new File(this.workingDirectory, this.pom.getFtlName()))
        ) {
            IOUtils.copy(input, outputStream);
            outputStream.flush();
        }
    }
}
