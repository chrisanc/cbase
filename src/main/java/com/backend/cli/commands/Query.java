package com.backend.cli.commands;

import com.backend.ai.adapters.*;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.LLM;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import com.backend.downloader.adapters.FileDownloaderImpl;
import com.backend.types.FilePath;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

/**
 * Picocli command class for querying indexed codebase vectors using local LLM inference.
 */
@Command(
        name = "query", description = "Executes a natural language query about the codebase using the local AI engine.",
        version = "v1.0.0", mixinStandardHelpOptions = true
)
public class Query implements Runnable {
    @Parameters(index = "0", description = "The natural language query to execute with the local LLM.")
    private String query;

    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();
    private final LLM<String, String> llm = new LLMImpl<>(
            String.class, String.class, FilePath.LOCAL_CACHE.getValue("models", "qwen"), new TranslatorImpl()
    );
    private final VectorDB database = new VectorDBImpl();

    /**
     * Default constructor for Query command.
     */
    public Query() {
    }

    /**
     * Executes the vector similarity lookup and local LLM reasoning pipeline.
     */
    @Override
    public void run() {
        com.backend.downloader.ports.FileDownloader downloader = new FileDownloaderImpl();
        com.backend.downloader.domain.ModelURL.MINILM.verifyOrDownload(downloader);

        Token token = tokenizer.tokenize(
                query, FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
        );
        float[] embedding = embeddings.embedTokens(token);
        
        String context = database.lookup(embedding);
        if (context == null || context.isBlank()) {
            System.out.println("[WARN] No indexed codebase context found.");
            System.out.println("Please run 'cbase scan -s' in your project root to build the vector index.");
            return;
        }

        System.out.println("\n[INFO] Relevant codebase context for query:\n");
        System.out.println(context);

        String prompt = "You are a senior software engineer acting as a programming mentor.\n" +
                "User Query:\n" + query + "\n\n" +
                "Codebase Context:\n" + context + "\n" +
                "Provide clear, actionable analysis and guidance strictly based on the context above.";

        System.out.println("[INFO] Generating local AI analysis...");
        try {
            com.backend.downloader.domain.ModelURL.QWEN.verifyOrDownload(downloader);
            String pred = this.llm.predict(prompt);
            System.out.println("\n[ANALYSIS] Local AI Output:\n" + pred);
        } catch (Exception e) {
            System.err.println("[WARN] Qwen local LLM inference encountered an issue. Displaying retrieved context above.");
        }
    }
}
