package com.backend.ai.ports;

import ai.djl.ndarray.NDList;
import com.backend.ai.domain.Token;

import java.util.List;

public interface Embeddings {
    /**
     * Takes raw tokens and gives them a meaning (creating embeddings)
     * */
    float[] embedTokens(Token tokens);
    public float[] meanPooling(NDList tensor, int vectorSize);
}