package com.backend.ai.ports;

/**
 * Port interface for local machine learning model execution and inference.
 *
 * @param <I> input type for prediction
 * @param <O> output type resulting from model inference
 */
public interface LLM<I, O> {
    /**
     * Executes forward-pass prediction/inference on the underlying model.
     *
     * @param input model input object
     * @return model output result object
     */
    O predict(I input);
}
