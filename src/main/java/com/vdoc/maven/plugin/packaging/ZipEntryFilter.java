package com.vdoc.maven.plugin.packaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface ZipEntryFilter
{
	boolean canFilter(File fileToZip);
	
	File filter(File fileToZip) throws IOException;
}
