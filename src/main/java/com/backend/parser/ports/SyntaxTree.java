package com.backend.parser.ports;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;

/**
 * Port interface for parsing Abstract Syntax Trees (AST) and analyzing cyclomatic complexity.
 */
public interface SyntaxTree {
    /**
     * Parses the Abstract Syntax Tree (AST) of the provided script and extracts methods.
     *
     * @param script {@link CodeScript} instance to analyze in-place
     */
    void analyze(CodeScript script);

    /**
     * Calculates the cyclomatic complexity score for a given parsed method.
     *
     * @param method {@link Method} instance to calculate complexity for
     */
    void calculateCyclicalComplexity(Method method);
}
