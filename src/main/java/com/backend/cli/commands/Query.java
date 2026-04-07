package com.backend.cli.commands;

import com.backend.ai.adapters.EmbeddingsImpl;
import com.backend.ai.adapters.TokenizerImpl;
import com.backend.ai.adapters.VectorDBImpl;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

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
        Token token = tokenizer.tokenize(query);
        float[] embedding = embeddings.embedTokens(token);

        String res = database.lookup(embedding);
        System.out.println("Results: " + res);
    }
}
