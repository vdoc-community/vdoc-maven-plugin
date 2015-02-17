package com.vdoc.maven.plugin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by famaridon on 04/07/2014.
 */
public class StreamGobbler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);

	protected GobblerAdapter gobblerAdapter;
	protected InputStream is;
	protected String prefix;

	public StreamGobbler(InputStream is, GobblerAdapter gobblerAdapter, String prefix) {
        super();
        this.is = is;
		this.prefix = prefix;
		this.gobblerAdapter = gobblerAdapter;
	}

	@Override
	public void run() {
		try (
				InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                this.gobblerAdapter.println(this.prefix + " > " + line);
            }

        } catch (IOException e) {
            LOGGER.error("Stream can't be gobbed!", e);
        }
	}
}