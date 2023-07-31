package com.etsubu.stonksbot.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Utility class for reading metadata from the application jar file
 *
 * @author etsubu
 */
public class ArchiveTools {
    private static final Logger log = LoggerFactory.getLogger(ArchiveTools.class);

    /**
     * Reads the application version number from the manifest.mf file in the jar archive
     *
     * @return Version number of the running application
     */
    public static Optional<String> getApplicationVersion() {
        InputStream input = ArchiveTools.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        try {
            Manifest manifest = new Manifest(input);
            Attributes attrs = manifest.getMainAttributes();
            return Optional.ofNullable(attrs.getValue("Version"));
        } catch (IOException e) {
            log.error("Failed to read version number from archive manifest", e);
        }
        return Optional.empty();
    }
}
