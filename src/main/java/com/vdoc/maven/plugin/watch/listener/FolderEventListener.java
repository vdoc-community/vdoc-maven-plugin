package com.vdoc.maven.plugin.watch.listener;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public interface FolderEventListener {
	public void onCreate(Path path);
	
	public void onDelete(Path path);
	
	public void onModify(Path path);
}
