package com.backend.cli.commands;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import com.backend.ai.adapters.EmbeddingsImpl;
import com.backend.ai.adapters.TokenizerImpl;
import com.backend.ai.adapters.TranslatorImpl;
import com.backend.ai.adapters.VectorDBImpl;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import com.backend.types.FilePath;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "query", description = "Executes a query about the codebase using a LLM. Must be in format \"query\"."
)
public class Query implements Runnable {
    @Parameters(index = "0", description = "The natural language query to execute with the local LLM.")
    private String query;
    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();
    private final VectorDB database = new VectorDBImpl();

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        // Tokenize and embed the query
        Token token = tokenizer.tokenize(
                query, FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json").toString()
        );
        float[] embedding = embeddings.embedTokens(token);
        // Generate the context for our coder LLM
        String res = database.lookup(embedding);
        // Finishing the context building
        res = "You're an senior software engineer who's the user programming mentor." +
                "As you're already too good at this, you give the best advices to him/her." +
                "He got some questions:\n" + query + "\n" + "The context is:" + res + "\n" +
                "Answer it's question ONLY. The question must be related to the context and " +
                "programming and, if it isn't, you gotta tell him/her to ask any other thing.\n" +
                "Always answer on the same language of the query.";

        // Load the model
        Criteria<String, String> qwen = Criteria
                .builder()
                .setTypes(String.class, String.class)
                .optModelPath(FilePath.LOCAL_CACHE.getValue("models", "qwen"))
                .optEngine("OnnxRuntime")
                .optProgress(new ProgressBar())
                .optTranslator(new TranslatorImpl())
                .build();

        // Execute the query
        try (
                ZooModel<String, String> model = qwen.loadModel();
                Predictor<String, String> predictor = model.newPredictor()
        ) {
            String out = predictor.predict(res);

            System.out.println(out);

        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            System.err.println("Error loading the model...");
            e.printStackTrace();
            System.exit(1);
        } catch (TranslateException e) {
            System.err.println("Error translating the query...");
            System.exit(1);
        }
    }
}
