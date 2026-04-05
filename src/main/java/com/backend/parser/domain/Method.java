package com.backend.parser.domain;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;

public class Method {
    private final String name;
    private final NodeList<Parameter> params;
    private final BlockStmt body;
    private int cyclicalComplexity;

    public Method(String name, NodeList<Parameter> params, BlockStmt body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public NodeList<Parameter> getParams() {
        return params;
    }

    public BlockStmt getBody() {
        return body;
    }

    public int getCyclicalComplexity() {
        return this.cyclicalComplexity;
    }

    public void setCyclicalComplexity(int complexity) {
        this.cyclicalComplexity = complexity;
    }

    @Override
    public String toString() {
        return "Method {\n" +
                "Name: " + this.name +
                "\nParams: " + this.params +
                "\nComplexity: " + this.cyclicalComplexity +
                "\n}";
    }
}
