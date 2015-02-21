package com.vdoc.maven.plugin.utils;

import com.vdoc.maven.plugin.utils.enums.LogLevel;

/**
 * Created by famaridon on 06/07/2014.
 */
public interface GobblerAdapter {
	public void println(String message);

    public void println(LogLevel level, String message);
}
