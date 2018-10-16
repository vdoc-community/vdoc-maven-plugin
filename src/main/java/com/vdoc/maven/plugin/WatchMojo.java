package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.utils.as.ApplicationServerContext;
import com.vdoc.maven.plugin.utils.project.ProjectContext;
import com.vdoc.maven.plugin.watch.WatchableSource;
import com.vdoc.maven.plugin.watch.WatcherRunnable;
import com.vdoc.maven.plugin.watch.listener.impl.deployer.DeployerEventListenerFactory;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this task is used to deploy a project to the target VDoc install.
 */
@Mojo(name = "watch", threadSafe = true,instantiationStrategy = InstantiationStrategy.SINGLETON)
public class WatchMojo extends AbstractDeployerVDocMojo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePluginDocMojo.class);

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {


		ProjectContext projectContext = this.findProjectContext();
		ApplicationServerContext applicationServerContext = this.findApplicationServerContext();


		ExecutorService executor = Executors.newCachedThreadPool();
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		Iterable<WatchableSource> watchableSources = projectContext.getWatchableSources();
		try {
			for (WatchableSource watchableSource : watchableSources) {
				WatcherRunnable runnable = new WatcherRunnable(watchableSource.getSource());
				runnable.addFolderEventListener(DeployerEventListenerFactory.newInstance(applicationServerContext,watchableSource));
				runnable.addExcludeMatchers(watchableSource.getExcludeMatchers());
				runnable.addExcludeMatcher("*___jb_*"); // exclude jetbrain files
				completionService.submit(runnable, "completed");
			}

			for (WatchableSource watchableSource : watchableSources) {
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

}
