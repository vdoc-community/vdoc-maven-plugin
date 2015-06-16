package com.vdoc.maven.plugin.utils;

/**
 * this class can be used to detect operating system.
 * Created by famaridon on 06/01/2015.
 */
public final class OSUtils {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private OSUtils() {
        super();
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || (OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return OS.contains("sunos");
    }
}
