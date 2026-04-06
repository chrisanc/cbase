package com.backend.ai.adapters;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of embeddings based on tokens
 * Using Java Deep Learning framework
 * */
public class EmbeddingsImpl implements Embeddings {
    /**
     * Takes raw tokens and gives them a meaning (creating embeddings)
     */
    @Override
    public float[] embedTokens(Token tokens) {
        // Load the model in-memory (model-in, model-out types)
        Criteria<NDList, NDList> criteria = Criteria
                .builder()
                .setTypes(NDList.class, NDList.class)
                .optModelPath(Path.of("C:/Users/Christian/Downloads/minilm/"))
                .optEngine("OnnxRuntime")
                .build();

        // Create tensors from raw arrays. Necessary for the embedding generation.
        NDList inputs;
        NDManager manager = NDManager.newBaseManager();
        // Create Arrays with another dimension
        NDArray ids = manager.create(tokens.getIds()).expandDims(0);
        NDArray attentionMask = manager.create(tokens.getAttentionMask()).expandDims(0);
        NDArray typeIds = manager.create(tokens.getTypeIds()).expandDims(0);
        // Create a list of NDArray
        inputs = new NDList(ids, attentionMask, typeIds);

        // Perform the forward-pass to get the embedding
        try (
                ZooModel<NDList, NDList> model = criteria.loadModel();
                Predictor<NDList, NDList> predictor = model.newPredictor()
        ) {
            // Get predictions
            NDList preds = predictor.predict(inputs);
            // Apply mean pooling to get a single vector
            return this.meanPooling(preds);
        } catch (IOException | ModelNotFoundException | MalformedModelException | TranslateException e) {
            System.err.println("Unexpected error while creating the embedding.");
            System.exit(1);
        }

        return new float[0];
    }

    public float[] meanPooling(NDList list) {
        // Define the array to return
        List<Float> results = new ArrayList<>();
        // Remove the batch dim (example: from (1, 4, 384) to (4, 384))
        // Useful because we need to iterate over the tokens embeddings
        try (NDArray arr = list.getFirst().squeeze()) {
            // Iterate through the first dimension (rows)
            long amountTokens = arr.getShape().get(0);
            for (int i = 0; i < amountTokens; i++) {
                results.add((arr.get(i).mean().getFloat()) / amountTokens);
            }
        }

        // Explicitly cast from List<Float> to float[] (no FloatStream available)
        float[] embedding = new float[results.size()];
        for (int i = 0; i < results.size(); i++) {
            embedding[i] = results.get(i);
        }

        return embedding;
    }
}
