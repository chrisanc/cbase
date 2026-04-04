package com.backend.parser.domain;

import java.util.List;

public final class CodeScript {
    private String path;
    private String name;
    private List<String> content;

    private CodeScript() {}

    public CodeScript(String path, String name, List<String> content) {
        this.path = path;
        this.name = name;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public List<String> getContent() {
        return content;
    }
}
