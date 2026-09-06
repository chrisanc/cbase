package com.backend.ai.adapters;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.LLM;
import com.backend.types.FilePath;

/**
 * Implementation of vector embeddings generation based on tokenized input,
 * utilizing the Deep Java Library (DJL) ONNX runtime engine.
 */
public class EmbeddingsImpl implements Embeddings {
    // Load the model in-memory (model-in, model-out types)
    private final LLM<NDList, NDList> llm = new LLMImpl<>(
            NDList.class, NDList.class, FilePath.LOCAL_CACHE.getValue("models", "minilm"), null
    );

    /**
     * Default constructor initializing EmbeddingsImpl.
     */
    public EmbeddingsImpl() {
    }

    /**
     * Takes raw tokens and generates a dense vector embedding using MiniLM model inference.
     *
     * @param tokens tokenized input representation
     * @return float array containing the 384-dimensional vector embedding
     */
    @Override
    public float[] embedTokens(Token tokens) {
        com.backend.downloader.domain.ModelURL.MINILM.verifyOrDownload(new com.backend.downloader.adapters.FileDownloaderImpl());
        // Create tensors from raw arrays. Necessary for the embedding generation.
        NDList inputs = this.buildTensor(tokens.getIds(), tokens.getAttentionMask(), tokens.getTypeIds());

        NDList predictions = this.llm.predict(inputs);

        return this.meanPooling(predictions, 384);
    }

    /**
     * Performs mean pooling across embedding tensors along dimension 0.
     *
     * @param tensor output NDList from model prediction
     * @param vectorSize expected embedding vector size
     * @return pooled float array embedding
     */
    @Override
    public float[] meanPooling(NDList tensor, int vectorSize) {
        try (NDArray arr = tensor.getFirst().squeeze()) {
            // Compute column-wise mean along dimension 0 natively
            try (NDArray pooled = arr.mean(new int[]{0})) {
                return pooled.toFloatArray();
            }
        }
    }

    /**
     * Helper method to construct DJL NDList tensors from variable long arrays.
     *
     * @param arrays array sequences to convert into NDList
     * @return constructed NDList tensor
     */
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