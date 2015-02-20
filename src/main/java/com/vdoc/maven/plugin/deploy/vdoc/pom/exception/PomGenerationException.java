package com.vdoc.maven.plugin.deploy.vdoc.pom.exception;

/**
 * Created by famaridon on 15/02/15.
 */
public class PomGenerationException extends Exception {
    public PomGenerationException() {
        super();
    }

    public PomGenerationException(String message) {
        super(message);
    }

    public PomGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PomGenerationException(Throwable cause) {
        super(cause);
    }

}
