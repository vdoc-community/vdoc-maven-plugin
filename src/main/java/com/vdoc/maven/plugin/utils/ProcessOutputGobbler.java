package com.vdoc.maven.plugin.utils;

import com.vdoc.maven.plugin.utils.enums.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Gob any process output to the specified {@link com.vdoc.maven.plugin.utils.GobblerAdapter}.<br>
 *     default stream if log into default gobblerAdapter {@link com.vdoc.maven.plugin.utils.enums.LogLevel}. The error stream is always log with LogLevel.ERROR
 * Created by famaridon on 04/07/2014.
 */
public class ProcessOutputGobbler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOutputGobbler.class);

    protected final GobblerAdapter gobblerAdapter;
    protected final Process process;

    public ProcessOutputGobbler(Process process, GobblerAdapter gobblerAdapter) {
        super();
        this.process = process;
        this.gobblerAdapter = gobblerAdapter;
    }

    @Override
    public void run() {
        new StreamGobbler(LogLevel.DEFAULT, this.process.getInputStream()).run();
        new StreamGobbler(LogLevel.ERROR, this.process.getErrorStream()).run();
    }

    private final class StreamGobbler extends Thread {

        private final LogLevel level;
        private final InputStream inputStream;

        private StreamGobbler(LogLevel level, InputStream inputStream) {
            super();
            this.level = level;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (
                    InputStreamReader isr = new InputStreamReader(this.inputStream);
                    BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    ProcessOutputGobbler.this.gobblerAdapter.println(line);
                }

            } catch (IOException e) {
                LOGGER.error("Stream can't be gobbed!", e);
            }
        }
    }
}