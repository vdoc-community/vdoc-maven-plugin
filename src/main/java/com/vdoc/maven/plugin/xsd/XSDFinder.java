package com.vdoc.maven.plugin.xsd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by famaridon on 25/02/15.
 */
public interface XSDFinder extends Callable<List<File>>, AutoCloseable {

    public void close() throws IOException;

}
