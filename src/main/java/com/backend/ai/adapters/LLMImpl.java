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

public class LLMImpl <I, O> implements LLM<I, O> {
    private final Criteria<I, O> criteria;
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
            System.err.println("Unexpected error while predicting.");
            System.exit(1);
        };

        return result;
    }
}
