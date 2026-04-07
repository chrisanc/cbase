package com.backend.ai.ports;

import com.backend.parser.domain.CodeScript;

import java.util.List;

public interface VectorDB {
    void save(CodeScript script);
    void saveAll(List<CodeScript> scripts);
    String lookup(float[] queryEmbedding);
    void closeDir();
}
