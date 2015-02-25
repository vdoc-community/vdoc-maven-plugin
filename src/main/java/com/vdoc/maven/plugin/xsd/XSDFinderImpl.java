package com.vdoc.maven.plugin.xsd;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by famaridon on 25/02/15.
 */
public class XSDFinderImpl implements XSDFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSDFinderImpl.class);

    private final File output;
    private final File jar;
    private final JarArchiveInputStream jarInputStream;
    private final FileInputStream jarStream;

    public XSDFinderImpl(File jar, File output) throws FileNotFoundException {
        super();
        this.jar = jar;
        this.output = output;

        // open jar stream
        this.jarStream = new FileInputStream(this.jar);
        this.jarInputStream = new JarArchiveInputStream(this.jarStream);

    }

    @Override
    public List<File> call() throws IOException {
        List<File> xsdList = new ArrayList<>();
        ArchiveEntry archiveEntry = this.jarInputStream.getNextEntry();
        while (null != archiveEntry) {
            if (archiveEntry.getName().endsWith(".xsd")) {
                xsdList.add(this.extract(archiveEntry));
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
        return xsdList;
    }

    private File extract(ArchiveEntry archiveEntry) throws IOException {
        String xsdName = StringUtils.substringAfterLast(archiveEntry.getName(), "/");
        File xsdFile = new File(this.output, xsdName);
        try (FileOutputStream xsdStream = new FileOutputStream(xsdFile)) {
            IOUtils.copyLarge(this.jarInputStream, xsdStream, 0, archiveEntry.getSize());
        }
        return xsdFile;
    }

    @Override
    public void close() throws IOException {
        // close jar stream
        this.jarInputStream.close();
        this.jarStream.close();
    }
}
