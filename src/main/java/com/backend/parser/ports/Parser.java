package com.backend.parser.ports;

import com.backend.parser.domain.CodeScript;

import java.util.List;

/**
 * Port interface for traversing the local project filesystem and parsing source code scripts.
 */
public interface Parser {
    /**
     * Traverses the project filesystem and reads supported source code files into memory.
     *
     * @return list of {@link CodeScript} instances representing discovered files
     */
    List<CodeScript> readFileSystem();

    /**
     * Parses AST structural information and cyclomatic metrics for the provided code scripts.
     *
     * @param scripts list of {@link CodeScript} objects to parse
     */
    void parse(List<CodeScript> scripts);
}
