package com.backend.parser.domain;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;

/**
 * Domain model representing a method parsed from source code, including name, parameters, AST body, and cyclomatic complexity.
 */
public class Method {
    private final String name;
    private final NodeList<Parameter> params;
    private final BlockStmt body;
    private int cyclicalComplexity;

    /**
     * Constructs a new Method instance.
     *
     * @param name name of the method
     * @param params AST {@link NodeList} of parameters
     * @param body AST {@link BlockStmt} representing the method body
     */
    public Method(String name, NodeList<Parameter> params, BlockStmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    /**
     * Gets the method name.
     *
     * @return method name string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the method parameters AST node list.
     *
     * @return {@link NodeList} of {@link Parameter} instances
     */
    public NodeList<Parameter> getParams() {
        return params;
    }

    /**
     * Gets the method body AST block statement.
     *
     * @return {@link BlockStmt} representing the method body
     */
    public BlockStmt getBody() {
        return body;
    }

    /**
     * Gets the cyclomatic complexity score calculated for this method.
     *
     * @return cyclomatic complexity integer score
     */
    public int getCyclicalComplexity() {
        return this.cyclicalComplexity;
    }

    /**
     * Sets the cyclomatic complexity score for this method.
     *
     * @param complexity cyclomatic complexity integer score
     */
    public void setCyclicalComplexity(int complexity) {
        this.cyclicalComplexity = complexity;
    }

    /**
     * Converts the method definition and metadata into a clean text chunk for vector embedding indexing.
     *
     * @param filePath path of the source file containing this method
     * @return formatted code chunk string
     */
    public String toChunkText(String filePath) {
        StringBuilder sb = new StringBuilder();
        sb.append("// File: ").append(filePath).append("\n");
        sb.append("// Method: ").append(name);
        if (params != null) {
            sb.append("(").append(params.toString()).append(")");
        } else {
            sb.append("()");
        }
        sb.append("\n// Cyclomatic Complexity: ").append(cyclicalComplexity).append("\n");
        if (body != null) {
            sb.append(body.toString());
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the Method instance.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "Method {\n" +
                "Name: " + this.name +
                "\nParams: " + this.params +
                "\nComplexity: " + this.cyclicalComplexity +
                "\n}";
    }
}
