package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by famaridon on 15/03/2017.
 */
public class VDocDeployerEventListener implements FolderEventListener
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VDocDeployerEventListener.class);

	protected final ApplicationServerContext applicationServerContext;
	protected final ProjectContext projectContext;

	public VDocDeployerEventListener(
			ApplicationServerContext applicationServerContext,
			ProjectContext projectContext) {
		this.applicationServerContext = applicationServerContext;
		this.projectContext = projectContext;
	}


	@Override
	public void onCreate(Path parent, Path relativePath)
	{
		try {
			Path from = parent.resolve(relativePath);
			Path to = this.getTo(relativePath);
			if (Files.isDirectory(from)) {
				LOGGER.debug("Create directory '{}'", to);
				FileUtils.forceMkdir(to.toFile());
			}
			else {
				LOGGER.debug("Create File '{}'", to);
				FileUtils.forceMkdir(to.getParent().toFile());
				Files.createFile(to);
			}
			LOGGER.info(" >> a : {}", to);
		}
		catch (IOException e) {
			LOGGER.error("Create '{}' fail", relativePath, e);
		}
	}
	
	@Override
	public void onDelete(Path parent, Path relativePath)
	{
		try {
			Path to = this.getTo(relativePath);
			if (Files.exists(to)) {
				if (Files.isDirectory(to)) {
					LOGGER.debug("Delete directory '{}'", to);
					FileUtils.deleteDirectory(to.toFile());
				}
				else {
					LOGGER.debug("Delete File '{}'", to);
					Files.delete(to);
				}
				LOGGER.info(" >> d : {}", to);
			}
		}
		catch (IOException e) {
			LOGGER.error("Delete '{}' fail", relativePath, e);
		}
	}
	
	@Override
	public void onModify(Path parent, Path relativePath)
	{
		try {
			Path from = parent.resolve(relativePath);
			Path to = this.getTo(relativePath);
			if (!Files.isDirectory(from)) {
				LOGGER.debug("Copy from '{}' to '{}'", from, to);
				FileUtils.forceMkdir(to.getParent().toFile());
				Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
			}
			LOGGER.info(" >> m : {}", to);
		}
		catch (IOException e) {
			LOGGER.error("Modify '{}' fail", relativePath, e);
		}
	}
	
	protected Path getTo(Path relativePath){
		return this.applicationServerContext.getEar().resolve(relativePath);
	}
}