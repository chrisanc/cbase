package com.backend.parser.domain;

import java.util.List;

/**
 * Domain model representing a source code file, its contents, path, and parsed methods.
 */
public class CodeScript {
    private final String path;
    private final String name;
    private final String content;
    private List<Method> methods;

    /**
     * Constructs a new CodeScript instance.
     *
     * @param path file system path of the code script
     * @param name file name of the code script
     * @param content raw text contents of the file
     */
    public CodeScript(String path, String name, String content) {
        this.path = path;
        this.name = name;
        this.content = content;
    }

    /**
     * Sets the list of methods extracted from this code script.
     *
     * @param methods list of parsed {@link Method} instances
     */
    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    /**
     * Gets the file system path of the script.
     *
     * @return file path string
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the name of the script file.
     *
     * @return file name string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the raw text content of the script.
     *
     * @return script content string
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the list of parsed methods within the script.
     *
     * @return list of {@link Method} instances
     */
    public List<Method> getMethods() {
        return methods;
    }

    /**
     * Returns a string representation of the CodeScript.
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return "CodeScript {\n" +
                "Path: " + this.path +
                "\nName: " + this.name +
                "\nMethods: " + this.methods +
                "\n}";
    }
}