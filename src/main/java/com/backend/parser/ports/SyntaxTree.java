package com.backend.parser.ports;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;

public interface SyntaxTree {
    /**
     * 'analyze' method uses AST to chunk and analyze the code, modifying
     * the script given in-place (non-returning method)
     * */
    void analyze(CodeScript script);
    /**
     * This method calculates the cyclical complexity of a method.
     * Implemented by the 'analyze' method itself.
     * */
    void calculateCyclicalComplexity(Method method);
}
