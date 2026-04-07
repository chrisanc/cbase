package com.backend.ai.adapters;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.types.FilePath;

import java.io.IOException;

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
                .optModelPath(FilePath.LOCAL_CACHE.getValue("models", "minilm"))
                .optEngine("OnnxRuntime")
                .build();

        // Perform the forward-pass to get the embedding
        try (
                ZooModel<NDList, NDList> model = criteria.loadModel();
                Predictor<NDList, NDList> predictor = model.newPredictor()
        ) {
            // Get predictions
            NDList preds = predictor.predict(tokens.getNDList());
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
        float[] embedding = new float[384];
        // Remove the batch dim (example: from (1, 4, 384) to (4, 384))
        // Useful because we need to iterate over the tokens embeddings
        try (NDArray arr = list.getFirst().squeeze()) {
            // Iterate through the first dimension (rows)
            long amountTokens = arr.getShape().get(0);
            long arrDim = arr.getShape().get(1);
            // Compact (pooling) the array where each position in the embedding
            // is equal to the average of each token in that pos (4, 384) -> (1, 384)
            for (int cols = 0; cols < arrDim; cols++) {
                float sum = 0;
                // Sum per column
                for (int rows = 0; rows < amountTokens; rows++) {
                    sum += arr.get(rows).get(cols).getFloat();
                }

                embedding[cols] = sum / amountTokens;
            }
        }

        return embedding;
    }
}
