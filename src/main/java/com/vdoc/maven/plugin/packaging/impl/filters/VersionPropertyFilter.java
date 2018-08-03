package com.vdoc.maven.plugin.packaging.impl.filters;

import com.vdoc.maven.plugin.packaging.ZipEntryFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class VersionPropertyFilter implements ZipEntryFilter
{
	
	private final String appsVersion;
	private final String encoding;
	
	public VersionPropertyFilter(String appsVersion, String encoding)
	{
		this.appsVersion = appsVersion;
		this.encoding = encoding;
	}
	
	@Override
	public boolean canFilter(File fileToZip)
	{
		return FilenameUtils.wildcardMatch(FilenameUtils.separatorsToUnix(fileToZip.getPath()), "*/packaging/*.xml");
	}
	
	@Override
	public File filter(File fileToZip) throws IOException
	{
		String content = IOUtils.toString(new FileInputStream(fileToZip), encoding);
		content = content.replaceAll("\\$\\{apps\\.version}", appsVersion);
		File tmpFileToZip = File.createTempFile(FilenameUtils.getBaseName(fileToZip.getName()), FilenameUtils.getExtension(fileToZip.getName()));
		try (FileOutputStream fos = new FileOutputStream(tmpFileToZip)) {
			IOUtils.write(content, fos, encoding);
		}
		tmpFileToZip.deleteOnExit();
		return tmpFileToZip;
	}
}
