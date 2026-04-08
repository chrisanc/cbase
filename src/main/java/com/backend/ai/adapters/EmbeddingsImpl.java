package com.backend.ai.adapters;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.LLM;
import com.backend.types.FilePath;

/**
 * Implementation of embeddings based on tokens
 * Using Java Deep Learning framework
 * */
public class EmbeddingsImpl implements Embeddings {
    // Load the model in-memory (model-in, model-out types)
    private final LLM<NDList, NDList> llm = new LLMImpl<>(
            NDList.class, NDList.class, FilePath.LOCAL_CACHE.getValue("models", "minilm"), null
    );
    /**
     * Takes raw tokens and gives them a meaning (creating embeddings)
     */
    @Override
    public float[] embedTokens(Token tokens) {
        // Create tensors from raw arrays. Necessary for the embedding generation.
        NDList inputs = this.buildTensor(tokens.getIds(), tokens.getAttentionMask(), tokens.getTypeIds());

        NDList predictions = this.llm.predict(inputs);

        return this.meanPooling(predictions, 384);
    }

    @Override
    public float[] meanPooling(NDList tensor, int vectorSize) {
        // Define a fixed-size vector
        float[] embedding = new float[vectorSize];
        // Remove singleton dims, keeping the important ones
        try (NDArray arr = tensor.getFirst().squeeze()) {
            long amountTokens = arr.getShape().get(0);
            // Get the sum per column, where 'amountTokens' are the rows
            for (int j = 0; j < vectorSize; j++) {
                float sum = 0;
                for (int i = 0; i < amountTokens; i++) {
                    sum += arr.get(i).get(j).getFloat();
                }

                embedding[j] = sum / amountTokens;
            }
        }

        return embedding;
    }

    private NDList buildTensor(long[]... arrays) {
        NDList tensor = new NDList();
        try (NDManager manager = NDManager.newBaseManager()) {
            for (long[] arr : arrays) {
                NDArray ndArr = manager.create(arr).expandDims(0);
                ndArr.detach();
                tensor.add(ndArr);
            }
        }
        tensor.detach();
        return tensor;
    }
}