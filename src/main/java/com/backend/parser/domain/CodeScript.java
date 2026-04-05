package com.backend.parser.domain;

import java.util.List;

public class CodeScript {
    private final String path;
    private final String name;
    private final String content;
    private List<Method> methods;

    public CodeScript(String path, String name, String content) {
        this.path = path;
        this.name = name;
        this.content = content;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public List<Method> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return "CodeScript {\n" +
                "Path: " + this.path +
                "\nName: " + this.name +
                "\nMethods: " + this.methods +
                "\n}";
    }
}