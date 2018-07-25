package com.vdoc.maven.plugin.watch;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import com.vdoc.maven.plugin.watch.listener.impl.AppsVDocHostDeployerEventListener;
import com.vdoc.maven.plugin.watch.listener.impl.CoreVDocHostDeployerEventListener;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public enum WatchType {
    APPS(AppsVDocHostDeployerEventListener.class),CORE(CoreVDocHostDeployerEventListener.class);

    private final Class<? extends FolderEventListener> folderEventListenerClass;

    WatchType(Class<? extends FolderEventListener> folderEventListenerClass) {
        this.folderEventListenerClass = folderEventListenerClass;
    }

    public FolderEventListener getFolderEventListener(Path source, Path target) {
        try {
            return  this.folderEventListenerClass.getDeclaredConstructor(Path.class,Path.class).newInstance(source,target);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
