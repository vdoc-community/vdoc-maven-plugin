package com.vdoc.maven.plugin.utils.impl;

import com.vdoc.maven.plugin.utils.GobblerAdapter;
import com.vdoc.maven.plugin.utils.enums.LogLevel;
import org.slf4j.Logger;

/**
 * An {@link GobblerAdapter} implementation for SLF4J
 * Created by famaridon on 06/07/2014.
 */
public class SLF4JLoggerAdapter implements GobblerAdapter {
    protected final Logger logger;
    protected final LogLevel level;
    protected final String prefix;

    public SLF4JLoggerAdapter(Logger log, String prefix) {
        this(log, LogLevel.DEBUG, prefix);
    }

    public SLF4JLoggerAdapter(Logger log, LogLevel level, String prefix) {
        super();
        this.logger = log;
        this.level = level;
        this.prefix = prefix;
    }

    @Override
    public void println(String message) {
        this.println(this.level, message);
    }

    @Override
    public void println(LogLevel level, String message) {
        switch (level) {
            case DEBUG:
                this.logger.debug("{} > {}", this.prefix, message);
                break;
            case INFO:
                this.logger.info("{} > {}", this.prefix, message);
                break;
            case ERROR:
                this.logger.error("{} > {}", this.prefix, message);
                break;
            case TRACE:
                this.logger.trace("{} > {}", this.prefix, message);
                break;
            case WARN:
                this.logger.warn("{} > {}", this.prefix, message);
                break;
            default:
                this.logger.debug("{} > {}", this.prefix, message);
        }

    }
}
