package com.backend.ai.adapters;

import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Tokenizer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TranslatorImpl implements Translator<String, String>{
    private final Tokenizer tokenizer = new TokenizerImpl();
    private List<String> vocabulary;

    /**
     * Prepares the translator with the manager and model to use.
     *
     * @param ctx the context for the {@code Predictor}.
     * @throws Exception if there is an error for preparing the translator
     */
    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        // Load the model and its json
        Model model = ctx.getModel();
        Path path = model.getModelPath().resolve("vocab.json");
        // Load the vocab in the model context
        this.vocabulary = Files.readAllLines(path);
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
        Token token = tokenizer.tokenize(input, "src/main/resources/models/qwen/tokenizer.json");
        return token.getNDList();
    }


    /**
     * Processes the output NDList to the corresponding output object.
     *
     * @param ctx  the toolkit used for post-processing
     * @param list the output NDList after inference, usually immutable in engines like
     *             PyTorch. @see <a href="https://github.com/deepjavalibrary/djl/issues/1774">Issue 1774</a>
     * @return the output object of expected type
     * @throws Exception if an error occurs during processing output
     */
    @Override
    public String processOutput(TranslatorContext ctx, NDList list) throws Exception {
        // Get the model tokens
        NDArray arr = list.singletonOrThrow();
        // Apply softmax function for probabilities and get the max
        arr = arr.softmax(0);
        long prediction = arr.argMax().getLong();
        arr.close();

        return vocabulary.get((int) prediction);
    }
}
