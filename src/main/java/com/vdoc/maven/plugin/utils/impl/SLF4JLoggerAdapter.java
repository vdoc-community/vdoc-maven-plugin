package com.vdoc.maven.plugin.utils.impl;

import com.vdoc.maven.plugin.utils.GobblerAdapter;
import org.slf4j.Logger;

/**
 * Created by famaridon on 06/07/2014.
 */
public class SLF4JLoggerAdapter implements GobblerAdapter {
    protected Logger logger;
    protected String level;

    public SLF4JLoggerAdapter(Logger log) {
        this(log, "info");
    }

    public SLF4JLoggerAdapter(Logger log, String level) {
        super();
        this.logger = log;
        this.level = level;
    }

    @Override
    public void println(String message) {
        switch (this.level) {
            case "debug":
                this.logger.debug(message);
                break;
            case "info":
                this.logger.info(message);
                break;
            case "error":
                this.logger.error(message);
                break;
            default:
                this.logger.debug(message);
        }

    }
}
