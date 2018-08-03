package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.watch.WatcherRunnable;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployementType;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployerEventListenerConfiguration;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployerEventListenerConfigurationBuilder;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployerEventListenerFactory;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * this task is used to deploy a project to the target VDoc install.
 */
@Mojo(name = "watch", threadSafe = true)
public class WatchMojo extends AbstractVDocMojo
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);
	
	/**
	 * the VDoc home folder.
	 */
	@Parameter
	protected File vdocHome;
	@Parameter
	protected List<File> sources;
	@Parameter
	protected List<String> excludes = new ArrayList<>();
	@Parameter(defaultValue = "")
	protected String targetPrefix ;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		
		this.initVDocHome();
		
		ApplicationServer applicationServer = this.detectApplicationServer();
		DeployementType deployementType = this.detectDeployementType();
		
		List<DeployerEventListenerConfiguration> configurations = new ArrayList<>();
		
		switch (deployementType){
			case APPS_CUSTOM:
			{
				List<Path> sources = this.findSourceCustoms();
				for (Path source : sources) {
					DeployerEventListenerConfiguration configuration = new DeployerEventListenerConfigurationBuilder()
						.setApplicationServer(applicationServer)
						.setDeployementType(deployementType)
						.setSource(source)
						.setTarget(Paths.get(this.vdocHome.toURI()))
						.createDeployerEventListenerConfiguration();
					configurations.add(configuration);
				}
			}
			break;
			case DIRECT:
			{
				List<File> sources = this.sources;
				for (File source : sources) {
					DeployerEventListenerConfiguration configuration = new DeployerEventListenerConfigurationBuilder()
						.setApplicationServer(applicationServer)
						.setDeployementType(deployementType)
						.setSource(Paths.get(source.toURI()))
						.setTarget(Paths.get(this.vdocHome.toURI()))
						.setTargetPrefix(Paths.get(this.targetPrefix))
						.createDeployerEventListenerConfiguration();
					configurations.add(configuration);
				}
			}
			break;
			default:
				throw new NotImplementedException("Deployment type not implemented");
		}
		
		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		try {
			for (DeployerEventListenerConfiguration configuration : configurations) {
				WatcherRunnable runnable = new WatcherRunnable(configuration.getSource());
				runnable.addFolderEventListener(DeployerEventListenerFactory.newInstance(configuration));
				runnable.addExcludeMatcher("*___jb_*"); // exclude jetbrain files
				for (String exclude : excludes) {
					runnable.addExcludeMatcher(exclude);
				}
				
				completionService.submit(runnable, "completed");
			}
			
			for (DeployerEventListenerConfiguration configuration : configurations) {
				completionService.take();
			}
		}
		catch (InterruptedException e) {
			// clean up state...
			Thread.currentThread().interrupt();
			throw new MojoFailureException("Can't wait for completion", e);
		}
		finally {
			executor.shutdown();
		}
		
	}
	
	private DeployementType detectDeployementType()
	{
		DeployementType deployementType;
		if (this.sources == null || this.sources.isEmpty()) {
			deployementType = DeployementType.APPS_CUSTOM;
		}
		else {
			deployementType = DeployementType.DIRECT;
		}
		return deployementType;
	}
	
	private ApplicationServer detectApplicationServer() throws MojoExecutionException
	{
		ApplicationServer applicationServer;
		if (new File(this.vdocHome, "JBoss").exists()) {
			applicationServer = ApplicationServer.JBOSS;
		}
		else if (new File(this.vdocHome, "wildfly").exists()) {
			applicationServer = ApplicationServer.WILDFLY;
		}
		else {
			throw new MojoExecutionException("application server not detected or not supported");
		}
		return applicationServer;
	}
	
	private void initVDocHome() throws MojoExecutionException
	{
		String vdocHomes = System.getenv("VDOC_HOMES");
		if (StringUtils.isNoneEmpty(vdocHomes)) {
			getLog().info("VDOC_HOMES found we use it (ignore vdocHome)");
			this.vdocHome = new File(vdocHomes, this.project.getVersion());
		}
		if ((this.vdocHome == null) || !this.vdocHome.exists() || this.getProject().getFile().equals(vdocHome)) {
			throw new MojoExecutionException("VDoc home not found or invalid path. " + vdocHome);
		}
		LOGGER.info("Target VDoc at {}", vdocHome);
	}
	
	private List<Path> findSourceCustoms()
	{
		LOGGER.info("Watch source folders : ");
		if ("pom".equalsIgnoreCase(this.project.getArtifact().getType())) {
			throw new NotImplementedException("");
		}
		else {
			List<String> sourceFolders = this.project.getCompileSourceRoots();
			return this.resolveSourceCustom(sourceFolders);
		}
	}
	
	private List<Path> resolveSourceCustom(List<String> sourceFolders)
	{
		List<Path> paths = new ArrayList<>(sourceFolders.size());
		for (String sourceFolder : sourceFolders) {
			Path sourceCustom = Paths.get(sourceFolder).getParent().resolve("custom");
			if (Files.exists(sourceCustom)) {
				LOGGER.info(" >> {}", sourceCustom);
				paths.add(sourceCustom);
			}
		}
		return paths;
	}
	
}
