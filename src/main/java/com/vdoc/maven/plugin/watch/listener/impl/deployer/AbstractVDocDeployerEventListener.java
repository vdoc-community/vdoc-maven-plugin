package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by famaridon on 15/03/2017.
 */
public abstract class AbstractVDocDeployerEventListener extends AbstractDeployerEventListener implements FolderEventListener
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVDocDeployerEventListener.class);
	
	protected final Path ear;
	protected final Path war;
	
	public AbstractVDocDeployerEventListener(DeployerEventListenerConfiguration configuration)
	{
		super(configuration);
		this.ear = this.resolveEar(this.configuration);
		this.war = this.ear.resolve("vdoc.war");
	}
	
	private Path resolveEar(DeployerEventListenerConfiguration configuration)
	{
		switch (configuration.getApplicationServer()){
			case JBOSS:
				return configuration.getTarget().resolve(Paths.get("JBoss", "server", "all", "deploy", "vdoc.ear"));
			case WILDFLY:
				return configuration.getTarget().resolve(Paths.get("wildfly", "standalone", "deployments", "vdoc.ear"));
			default:
				throw new NotImplementedException("Application server type not implemented");
		}
	}
	
	protected abstract Path getTo(Path path);

}