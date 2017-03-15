package com.vdoc.maven.plugin.watch;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import com.vdoc.maven.plugin.watch.listener.impl.LoggerEventListener;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by famaridon on 15/03/2017.
 */
public class WatcherRunnable implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(WatcherRunnable.class);
	private final Path watchedFolder;
	private WatchService folderWatcher;
	
	private boolean running = true;
	
	private List<String> excludeMatcherList = new ArrayList<>();
	private List<FolderEventListener> folderEventListenerList = new ArrayList<>();
	
	public WatcherRunnable(Path sourceFolder) {
		this.watchedFolder = sourceFolder;
	}
	
	@Override
	public void run() {
		
		folderEventListenerList.add(0,new LoggerEventListener());
		
		try {
			folderWatcher = watchedFolder.getFileSystem().newWatchService();
			// register directory and sub-directories
			this.registerFolder(watchedFolder);
			
		} catch (IOException e) {
			throw new IllegalArgumentException("Configuration fail folder can't be scanned!", e);
		}
		
		try {
			
			boolean valid = true;
			while (valid && running) {
				// take suspend thread within events
				WatchKey watchKey = folderWatcher.poll(250, TimeUnit.MILLISECONDS);
				if(watchKey == null){
					continue; // used to check for stop
				}
				List<WatchEvent<?>> events = watchKey.pollEvents();
				for (WatchEvent event : events) {
					notifyListeners(watchKey, event);
				}
				valid = watchKey.reset();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException("fail to tack watch!", e);
		}
		finally {
			try {
				folderWatcher.close();
			} catch (IOException e) {
				LOGGER.error("watcher close fail.",e);
			}
		}
	}
	
	private void notifyListeners(WatchKey watchKey, WatchEvent<Path> event) {
		// resolve the path with the parent folder
		Path folder = ((Path) watchKey.watchable()).resolve(event.context());
		Path relativeFolderPath = watchedFolder.relativize(folder);
		if(this.isExcludeFolder(relativeFolderPath)){
			return;
		}
		
		for (FolderEventListener folderEventListener : folderEventListenerList) {
			if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
				if (Files.isDirectory(folder)) {
					registerFolder(folder);
				}
				folderEventListener.onCreate(relativeFolderPath);
			} else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
				folderEventListener.onDelete(relativeFolderPath);
			} else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
				folderEventListener.onModify(relativeFolderPath);
			}
		}
	}
	
	private void registerFolder(Path dir) {
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					LOGGER.debug("Watch for : {}", dir.toString());
					dir.register(folderWatcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new IllegalArgumentException("Folder '" + dir.toString() + "' can't be watched!", e);
		}
	}
	
	private boolean isExcludeFolder(Path relativePath){
		for(String matcher : excludeMatcherList){
			if(FilenameUtils.wildcardMatch(relativePath.toString(),matcher)){
				return true;
			}
		}
		return false;
	}
	
	public List<FolderEventListener> getFolderEventListenerList() {
		return folderEventListenerList;
	}
	
	public boolean addFolderEventListener(FolderEventListener listener) {
		return folderEventListenerList.add(listener);
	}
	
	public List<String> getExcludeMatcherList() {
		return excludeMatcherList;
	}
	
	public boolean addExcludeMatcher(String matcher) {
		return excludeMatcherList.add(matcher);
	}
	
	public void stopWatching() {
		LOGGER.debug("Watcher stop received !");
		synchronized (this) {
			Thread.yield();
			this.running = false;
		}
	}
}
