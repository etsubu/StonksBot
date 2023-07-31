package com.etsubu.stonksbot.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class VersionParser {
    private static final Logger log = LoggerFactory.getLogger(VersionParser.class);
    private static final String DEFAULT_VERSION = "0";

    public static String applicationVersion() {
        Enumeration<URL> resources;
        try {
            resources = VersionParser.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            log.error("Failed to read manifest file. Returning default version", e);
            return DEFAULT_VERSION;
        }
        if (resources.hasMoreElements()) {
            try {
                try (InputStream is = resources.nextElement().openStream()) {
                    Manifest manifest = new Manifest(is);
                    Attributes attr = manifest.getAttributes("Version");
                    if (attr == null || !attr.containsKey("Version")) {
                        log.error("No version number in manifest. Returning default version");
                        return DEFAULT_VERSION;
                    }
                    return attr.getValue("Version");
                }
            } catch (IOException e) {
                log.error("Failed to read manifest resource. Returning default version", e);
                return DEFAULT_VERSION;
            }
        }
        return DEFAULT_VERSION;
    }
}
