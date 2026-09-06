package com.backend.ai.ports;

import ai.djl.ndarray.NDList;
import com.backend.ai.domain.Token;

import java.util.List;

/**
 * Port interface for creating vector embeddings from tokenized input and performing tensor operations.
 */
public interface Embeddings {
    /**
     * Converts tokenized input into dense vector embedding representations.
     *
     * @param tokens tokenized representation of text input
     * @return float array containing the generated vector embedding
     */
    float[] embedTokens(Token tokens);

    /**
     * Performs mean pooling operations across output tensors to produce a fixed-size vector embedding.
     *
     * @param tensor tensor NDList produced by model inference
     * @param vectorSize expected output vector dimension size
     * @return pooled float array embedding
     */
    float[] meanPooling(NDList tensor, int vectorSize);
}