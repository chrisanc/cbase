package com.backend.cli.commands;

import com.backend.ai.adapters.*;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.LLM;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import com.backend.types.FilePath;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

@Command(
        name = "query", description = "Executes a query about the codebase using a LLM. Must be in format \"query\"."
)
public class Query implements Runnable {
    @Parameters(index = "0", description = "The natural language query to execute with the local LLM.")
    private String query;
    // Set the dependencies
    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();
    private final LLM<String, String> llm = new LLMImpl<>(
            String.class, String.class, FilePath.LOCAL_CACHE.getValue("models", "qwen"), new TranslatorImpl()
    );
    private final VectorDB database = new VectorDBImpl();

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        // Tokenize and embed the query
        Token token = tokenizer.tokenize(
                query, FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
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

        String pred = this.llm.predict(res);
        System.out.println(pred);
    }
}
