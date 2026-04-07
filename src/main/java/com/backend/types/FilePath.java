package com.backend.types;

import java.nio.file.Path;

public enum FilePath {
    LOCAL_CACHE(Path.of(System.getProperty("user.home"), ".cbase")),
    WDIR_CACHE(Path.of(System.getProperty("user.dir"), ".cbase"));

    private final Path value;
    FilePath(Path value) {
        this.value = value;
    }

    private Path getValue() {
        return value;
    }

    public Path getValue(String... other) {
        return Path.of(this.value.toString(), other);
    }
}
