package com.vdoc.maven.plugin.deploy.vdoc.spliter;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * this implementation split jar whose contain all (bin, javadoc, and source) into multiple jar like maven default output.
 * Created by famaridon on 10/02/15.
 */
public class JarSplitterImpl implements JarSplitter {

    private final File jar;
    private final JarArchiveInputStream jarInputStream;
    private final FileInputStream jarStream;
    private final File javadoc;
    private final ZipArchiveOutputStream javadocOutputStream;
    private final File source;
    private final ZipArchiveOutputStream sourceOutputStream;

    public JarSplitterImpl(File jar) throws IOException, FileNotFoundException {
        super();
        this.jar = jar;
        this.javadoc = new File(jar.getParentFile(), FilenameUtils.getBaseName(jar.getName()) + "-javadoc.jar");
        this.source = new File(jar.getParentFile(), FilenameUtils.getBaseName(jar.getName()) + "-source.jar");

        // open all stream
        // the jar jar
        this.jarStream = new FileInputStream(this.jar);
        this.jarInputStream = new JarArchiveInputStream(this.jarStream);

        ArchiveEntry root = new ZipArchiveEntry("/");
        // the javadoc jar
        this.javadocOutputStream = new ZipArchiveOutputStream(this.javadoc);
        this.javadocOutputStream.putArchiveEntry(root);
        this.javadocOutputStream.closeArchiveEntry();

        // the source jar
        this.sourceOutputStream = new ZipArchiveOutputStream(this.source);
        this.sourceOutputStream.putArchiveEntry(root);
        this.sourceOutputStream.closeArchiveEntry();

    }

    public void split() throws IOException {

        ArchiveEntry archiveEntry = this.jarInputStream.getNextEntry();
        while (null != archiveEntry) {
            if (archiveEntry.getName().startsWith("apidocs/") && !archiveEntry.getName().equals("apidocs/")) {
                this.appendJavadoc(archiveEntry);
            } else if (archiveEntry.getName().endsWith(".java")) {
                this.appendSource(archiveEntry);
            } else {
//                getLog().debug("class : " + archiveEntry.getName());
                if (archiveEntry.getSize() > 0) {
                    if (archiveEntry.getSize() != this.jarInputStream.skip(archiveEntry.getSize())) {
                        throw new IllegalStateException("the archive reader cursor have not skip the right number of byte!");
                    }
                }
            }
            archiveEntry = this.jarInputStream.getNextEntry();
        }

    }

    protected void appendJavadoc(ArchiveEntry jarEntry) throws IOException {
        //                getLog().debug("javadoc : " + archiveEntry.getName());
        ZipArchiveEntry toEntry = new ZipArchiveEntry(StringUtils.substringAfter(jarEntry.getName(), "apidocs/"));
        toEntry.setSize(jarEntry.getSize());
        this.copyEntry(this.javadocOutputStream, jarEntry, toEntry);
    }

    protected void appendSource(ArchiveEntry jarEntry) throws IOException {
//                getLog().debug("source : " + archiveEntry.getName());
        ZipArchiveEntry toEntry = new ZipArchiveEntry(jarEntry.getName());
        toEntry.setSize(jarEntry.getSize());
        this.copyEntry(this.sourceOutputStream, jarEntry, toEntry);
    }

    protected void copyEntry(ArchiveOutputStream toStream, ArchiveEntry jarEntry, ArchiveEntry toEntry) throws IOException {
//        getLog().debug("merge entry : " + entry.getName());
        toStream.putArchiveEntry(toEntry);
        if (jarEntry.getSize() > 0) {
            long copied = IOUtils.copyLarge(this.jarInputStream, toStream, 0, jarEntry.getSize());
            if (copied != jarEntry.getSize()) {
                throw new IOException("Zip entry is not fully copied!!");
            }
        }
        toStream.closeArchiveEntry();
    }

    public File getJar() {
        return this.jar;
    }

    public File getJavadoc() {
        return this.javadoc;
    }

    public File getSource() {
        return this.source;
    }

    @Override
    public void close() throws IOException {
        // close all stream
        // the jar jar
        this.jarInputStream.close();
        this.jarStream.close();

        // the javadoc jar
        this.javadocOutputStream.close();

        // the source jar
        this.sourceOutputStream.close();
    }
}
