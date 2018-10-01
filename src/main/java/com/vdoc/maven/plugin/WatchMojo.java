package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.watch.WatcherRunnable;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployerEventListenerFactory;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * this task is used to deploy a project to the target VDoc install.
 */
@Mojo(name = "watch", threadSafe = true)
public class WatchMojo extends AbstractDeployerVDocMojo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);

	protected List<Path> sourceCustoms = new ArrayList<>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {


		ProjectContext projectContext = this.findProjectContext();
		ApplicationServerContext applicationServerContext = this.findApplicationServerContext();

		this.initSourceCustoms();

		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		try {
			for (Path sourceCustom : this.sourceCustoms) {
				WatcherRunnable runnable = new WatcherRunnable(sourceCustom);
				runnable.addFolderEventListener(DeployerEventListenerFactory.newInstance(applicationServerContext,projectContext));
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
			//Addon
			addSourceCustom(sourceFolder, "custom");
			//VDodWAR
			addSourceCustom(sourceFolder, "webapp");
		}
	}

	private void addSourceCustom(String sourceFolder, String folderName) {
		Path path = Paths.get(sourceFolder).getParent().resolve(folderName);
		if (Files.exists(path)) {
			LOGGER.info(" >> {}", path);
			this.sourceCustoms.add(path);
		}
	}

}
