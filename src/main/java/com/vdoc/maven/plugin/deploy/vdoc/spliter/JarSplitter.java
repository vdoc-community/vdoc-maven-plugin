package com.vdoc.maven.plugin.deploy.vdoc.spliter;

import java.io.File;
import java.io.IOException;

/**
 * Created by famaridon on 10/02/15.
 */
public interface JarSplitter extends AutoCloseable {

    /**
     * try to split jar into 3 jar (jar, javadoc, source)
     *
     * @throws IOException
     */
    public void split() throws IOException;

    /**
     * get the split result jar file
     *
     * @return
     */
    public File getJar();

    /**
     * get the split result javadoc file
     *
     * @return
     */
    public File getJavadoc();

    /**
     * get the split result source file
     *
     * @return
     */
    public File getSource();

    @Override
    void close() throws IOException;
}
