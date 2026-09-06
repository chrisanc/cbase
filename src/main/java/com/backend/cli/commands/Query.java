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
        // Auto-verify model dependencies
        com.backend.downloader.ports.FileDownloader downloader = new FileDownloaderImpl();
        com.backend.downloader.domain.ModelURL.MINILM.verifyOrDownload(downloader);

        // Tokenize and embed the query
        Token token = tokenizer.tokenize(
                query, FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
        );
        float[] embedding = embeddings.embedTokens(token);
        
        // Retrieve vector context from database
        String context = database.lookup(embedding);
        if (context == null || context.isBlank()) {
            System.out.println("⚠️ No indexed codebase context found.");
            System.out.println("Please run 'cbase scan -s' in your project root to build the vector index.");
            return;
        }

        System.out.println("\n🔍 Found relevant code context for your query:\n");
        System.out.println(context);

        String prompt = "You're a senior software engineer acting as a programming mentor.\n" +
                "User Query:\n" + query + "\n\n" +
                "Codebase Context:\n" + context + "\n" +
                "Provide clear, actionable analysis and guidance strictly based on the context above.";

        System.out.println("🤖 Generating AI advice...");
        try {
            com.backend.downloader.domain.ModelURL.QWEN.verifyOrDownload(downloader);
            String pred = this.llm.predict(prompt);
            System.out.println("\n💡 AI Advice:\n" + pred);
        } catch (Exception e) {
            System.err.println("Note: Qwen local LLM inference encountered an issue. Displaying retrieved context above.");
        }
    }
}
