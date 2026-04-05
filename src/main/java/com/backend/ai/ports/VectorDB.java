package com.backend.ai.ports;

import com.backend.parser.domain.CodeScript;

public interface VectorDB {
    void save(CodeScript script);
    void lookup(String path);
}
