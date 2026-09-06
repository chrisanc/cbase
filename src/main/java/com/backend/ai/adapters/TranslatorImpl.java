package com.backend.ai.adapters;

import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Tokenizer;
import com.backend.types.FilePath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of DJL {@link Translator} interface for pre-processing string prompts into tensors
 * and post-processing model logit outputs into text responses.
 */
public class TranslatorImpl implements Translator<String, String> {
    private final Tokenizer tokenizer = new TokenizerImpl();
    private List<String> vocabulary;

    /**
     * Default constructor for TranslatorImpl.
     */
    public TranslatorImpl() {
    }

    /**
     * Prepares the translator with the manager and model to use.
     *
     * @param ctx the context for the {@code Predictor}.
     * @throws Exception if there is an error for preparing the translator
     */
    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        // Preparation hook if model assets need pre-loading
    }

    /**
     * Processes the input and converts it to NDList.
     *
     * @param ctx   the toolkit for creating the input NDArray
     * @param input the input object
     * @return the {@link NDList} after pre-processing
     * @throws Exception if an error occurs during processing input
     */
    @Override
    public NDList processInput(TranslatorContext ctx, String input) throws Exception {
        Token token = this.tokenizer.tokenize(
                input, FilePath.LOCAL_CACHE.getValue("models", "qwen", "tokenizer.json")
        );
        return token.getNDList();
    }

    /**
     * Processes the output NDList to the corresponding output object.
     *
     * @param ctx  the toolkit used for post-processing
     * @param list the output NDList after inference
     * @return the output object of expected type
     * @throws Exception if an error occurs during processing output
     */
    @Override
    public String processOutput(TranslatorContext ctx, NDList list) throws Exception {
        NDArray arr = list.get(0);
        long predictedTokenId;
        
        long[] shape = arr.getShape().getShape();
        if (shape.length == 3) {
            // [batch, seq_len, vocab_size] -> last token logits
            long seqLen = shape[1];
            try (NDArray lastTokenLogits = arr.get(0).get(seqLen - 1)) {
                predictedTokenId = lastTokenLogits.argMax().getLong();
            }
        } else if (shape.length == 2) {
            // [seq_len, vocab_size]
            long seqLen = shape[0];
            try (NDArray lastTokenLogits = arr.get(seqLen - 1)) {
                predictedTokenId = lastTokenLogits.argMax().getLong();
            }
        } else {
            predictedTokenId = arr.argMax().getLong();
        }

        Path tokenizerPath = FilePath.LOCAL_CACHE.getValue("models", "qwen", "tokenizer.json");
        return this.tokenizer.decode(new long[]{predictedTokenId}, tokenizerPath);
    }
}
