package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.watch.WatcherRunnable;
import com.vdoc.maven.plugin.watch.listener.impl.VDocHostDeployerEventListener;
import org.apache.commons.lang3.NotImplementedException;
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
import java.nio.file.WatchService;
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
public class WatchMojo extends AbstractVDocMojo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);
	
	protected Path targetEar;
	protected Path targetCustom;
	protected List<Path> sourceCustoms = new ArrayList<>();
	protected WatchService watcher;
	
	/**
	 * the VDoc home folder.
	 */
	@Parameter(required = true)
	protected File vdocHome;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		this.getProject().getFile();
		if ((this.vdocHome == null) || !this.vdocHome.exists() || this.getProject().getFile().equals(vdocHome)) {
			LOGGER.error("VDoc home not found or invalid path.");
			return;
		}
		LOGGER.info("Target VDoc at {}", vdocHome);
		
		this.initSourceCustoms();
		
		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		try {
			for (Path sourceCustom : this.sourceCustoms) {
				WatcherRunnable runnable = new WatcherRunnable(sourceCustom);
				runnable.addFolderEventListener(new VDocHostDeployerEventListener(sourceCustom, Paths.get(this.vdocHome.toURI())));
				runnable.addExcludeMatcher("*___jb_*"); // exclude jetbrain files
				
				completionService.submit(runnable, "completed");
			}
			
			for (Path sourceCustom : this.sourceCustoms) {
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
	
	private void initSourceCustoms() {
		LOGGER.info("Watch source folders : ");
		if ("pom".equalsIgnoreCase(this.project.getArtifact().getType())) {
			throw new NotImplementedException("");
		}
		else {
			List<String> sourceFolders = this.project.getCompileSourceRoots();
			addSourceCustom(sourceFolders);
		}
	}
	
	private void addSourceCustom(List<String> sourceFolders) {
		for (String sourceFolder : sourceFolders) {
			Path sourceCustom = Paths.get(sourceFolder).getParent().resolve("custom");
			if (Files.exists(sourceCustom)) {
				LOGGER.info(" >> {}", sourceCustom);
				sourceCustoms.add(sourceCustom);
			}
		}
	}
	
}
