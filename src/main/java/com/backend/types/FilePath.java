package com.backend.types;

import java.nio.file.Path;

/**
 * Enumeration representing standard application directory paths (user home cache and working directory cache).
 */
public enum FilePath {
    /** Root path for application cache in user's home directory (~/.cbase). */
    LOCAL_CACHE(Path.of(System.getProperty("user.home"), ".cbase")),
    
    /** Root path for application cache in the current working directory (./.cbase). */
    WDIR_CACHE(Path.of(System.getProperty("user.dir"), ".cbase"));

    private final Path value;

    FilePath(Path value) {
        this.value = value;
    }

    /**
     * Internal getter for base path.
     *
     * @return base {@link Path}
     */
    private Path getValue() {
        return value;
    }

    /**
     * Resolves a child path relative to the base directory.
     *
     * @param other path segments to append to the base directory path
     * @return resolved {@link Path} object
     */
    public Path getValue(String... other) {
        return Path.of(this.value.toString(), other);
    }
}
