package com.vdoc.maven.plugin.deploy.vdoc.pom;

import com.vdoc.maven.plugin.deploy.vdoc.beans.Artifact;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Maven;
import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by famaridon on 15/02/15.
 */
public class ParentPOMGeneratorImpl implements ParentPOMGenerator
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ParentPOMGeneratorImpl.class);
	public static final Version fmVersion = Configuration.VERSION_2_3_20;
	
	private final Configuration configuration;
	private final Maven maven;
	private final ParentPOM pom;
	private final String targetVersion;
	private final Path tempDirectory;
	
	public ParentPOMGeneratorImpl(Maven maven,ParentPOM pom, String targetVersion) throws IOException
	{
		super();
		this.maven = maven;
		this.pom = pom;
		this.targetVersion = targetVersion;
		
		tempDirectory = Files.createTempDirectory("vdoc-maven-plugin");
		tempDirectory.toFile().deleteOnExit();
		
		// build the full pom
		this.configuration = new Configuration(ParentPOMGeneratorImpl.fmVersion);
		
		// Specify the data source where the template files come from.
		this.configuration.setDirectoryForTemplateLoading(tempDirectory.toFile());
		
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
	public File generate() throws PomGenerationException
	{
		try
		{
			this.copyTemplateLocaly();
			Template temp = this.configuration.getTemplate(this.pom.getFtlName());
			
			File pomFile = new File(this.tempDirectory.toFile(), "pom.xml");
			try (Writer out = new FileWriter(pomFile))
			{
				temp.process(this, out);
				out.flush();
			}
			return pomFile;
		}
		catch (IOException | TemplateException e)
		{
			throw new PomGenerationException(e);
		}
	}
	
	public void copyTemplateLocaly() throws IOException
	{
		LOGGER.debug("Extract {} file into {}", this.pom.getFtlName(), this.tempDirectory);
		try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("pom/" + this.pom.getFtlName());
		     FileOutputStream outputStream = new FileOutputStream(new File(this.tempDirectory.toFile(), this.pom.getFtlName())))
		{
			IOUtils.copy(input, outputStream);
			outputStream.flush();
		}
	}
	
	/**
	 * get {@link ParentPOMGeneratorImpl#configuration} property
	 *
	 * @return get the configuration property
	 **/
	public Configuration getConfiguration()
	{
		return configuration;
	}
	
	/**
	 * get {@link ParentPOMGeneratorImpl#maven} property
	 *
	 * @return get the maven property
	 **/
	public Maven getMaven()
	{
		return maven;
	}
	
	/**
	 * get {@link ParentPOMGeneratorImpl#pom} property
	 *
	 * @return get the pom property
	 **/
	public ParentPOM getPom()
	{
		return pom;
	}
	
	/**
	 * get {@link ParentPOMGeneratorImpl#targetVersion} property
	 *
	 * @return get the targetVersion property
	 **/
	public String getTargetVersion()
	{
		return targetVersion;
	}
	
	/**
	 * get {@link ParentPOMGeneratorImpl#tempDirectory} property
	 *
	 * @return get the tempDirectory property
	 **/
	public Path getTempDirectory()
	{
		return tempDirectory;
	}
}
