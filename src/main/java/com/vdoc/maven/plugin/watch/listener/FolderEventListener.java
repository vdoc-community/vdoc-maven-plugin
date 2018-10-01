package com.vdoc.maven.plugin.watch.listener;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public interface FolderEventListener {
	public void onCreate(Path parent, Path relativePath);
	
	public void onDelete(Path parent, Path relativePath);
	
	public void onModify(Path parent, Path relativePath);
}
