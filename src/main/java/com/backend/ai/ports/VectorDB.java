package com.backend.ai.ports;

import com.backend.parser.domain.CodeScript;

import java.util.List;

/**
 * Port interface for indexing code scripts as vector embeddings and querying nearest neighbor code matches.
 */
public interface VectorDB {
    /**
     * Indexes a single code script into the vector database.
     *
     * @param script {@link CodeScript} instance to index
     */
    void save(CodeScript script);

    /**
     * Indexes a list of code scripts into the vector database.
     *
     * @param scripts list of {@link CodeScript} instances to index
     */
    void saveAll(List<CodeScript> scripts);

    /**
     * Performs a k-nearest neighbor (k-NN) similarity search using the query vector embedding.
     *
     * @param queryEmbedding float array representing query vector embedding
     * @return formatted string containing relevant code snippets and metadata
     */
    String lookup(float[] queryEmbedding);

    /**
     * Closes the vector database index directory and releases underlying storage resources.
     */
    void closeDir();
}
