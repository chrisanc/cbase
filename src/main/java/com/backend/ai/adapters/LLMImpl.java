package com.backend.ai.adapters;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import com.backend.ai.ports.LLM;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of the {@link LLM} port interface using Deep Java Library (DJL) ONNX runtime.
 *
 * @param <I> input object type
 * @param <O> prediction output type
 */
public class LLMImpl<I, O> implements LLM<I, O> {
    private final Criteria<I, O> criteria;

    /**
     * Constructs an LLMImpl instance configured with input/output classes, model path, and optional translator.
     *
     * @param in input class type
     * @param out output class type
     * @param modelPath path to the directory containing model assets
     * @param translator optional custom DJL {@link Translator} instance
     */
    public LLMImpl(Class<I> in, Class<O> out, Path modelPath, Translator<I, O> translator) {
        var builder = Criteria
                .builder()
                .setTypes(in, out)
                .optModelPath(modelPath)
                .optEngine("OnnxRuntime");

        if (translator != null) {
            builder.optTranslator(translator);
        }

        this.criteria = builder.build();
    }

    /**
     * Loads the model, creates a predictor, and executes forward-pass prediction.
     *
     * @param input model input object
     * @return model output prediction result
     * @throws RuntimeException if model loading or prediction encounters an error
     */
    @Override
    public O predict(I input) {
        O result = null;
        // Perform the forward-pass to get the embedding
        try (
                ZooModel<I, O> model = this.criteria.loadModel();
                Predictor<I, O> predictor = model.newPredictor()
        ) {
            // Get predictions
            result = predictor.predict(input);
        } catch (IOException | ModelNotFoundException | MalformedModelException | TranslateException e) {
            System.err.println("[ERROR] Unexpected error while predicting: " + e.getMessage());
            throw new RuntimeException("Model prediction failed", e);
        }

        return result;
    }
}
