package com.backend.ai.adapters;

import com.backend.types.FilePath;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import com.backend.parser.domain.CodeScript;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementation of the {@link VectorDB} port using Apache Lucene for storing, indexing,
 * and performing k-nearest neighbor (k-NN) vector similarity searches.
 */
public class VectorDBImpl implements VectorDB {
    private final Directory directory = getDirectory(FilePath.WDIR_CACHE.getValue("db").toString());
    private final Analyzer analyzer = new StandardAnalyzer();
    private final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    private final IndexWriter writer = getWriter();

    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();

    /**
     * Default constructor for VectorDBImpl.
     */
    public VectorDBImpl() {
    }

    /**
     * Indexes both file-level and method-level symbol documents with corresponding vector embeddings.
     *
     * @param script {@link CodeScript} instance containing source code and extracted methods
     */
    @Override
    public void save(CodeScript script) {
        // 1. Index file-level document summary
        Document fileDoc = new Document();
        fileDoc.add(new StoredField("path", script.getPath()));
        fileDoc.add(new StoredField("symbol", script.getName()));
        fileDoc.add(new StoredField("type", "file"));
        fileDoc.add(new StoredField("complexity", "0"));
        fileDoc.add(new StoredField("content", script.getContent()));
        fileDoc.add(
            new KnnFloatVectorField(
                "embedding",
                embeddings.embedTokens(tokenizer.tokenize(
                    script.getContent(),
                    FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
                ))
            )
        );
        this.addDocument(fileDoc);

        // 2. Index method-level symbol documents
        if (script.getMethods() != null) {
            for (com.backend.parser.domain.Method method : script.getMethods()) {
                String chunkText = method.toChunkText(script.getPath());
                Document methodDoc = new Document();
                methodDoc.add(new StoredField("path", script.getPath()));
                methodDoc.add(new StoredField("symbol", method.getName()));
                methodDoc.add(new StoredField("type", "method"));
                methodDoc.add(new StoredField("complexity", String.valueOf(method.getCyclicalComplexity())));
                methodDoc.add(new StoredField("content", chunkText));
                methodDoc.add(
                    new KnnFloatVectorField(
                        "embedding",
                        embeddings.embedTokens(tokenizer.tokenize(
                            chunkText,
                            FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
                        ))
                    )
                );
                this.addDocument(methodDoc);
            }
        }
    }

    /**
     * Indexes a list of code scripts into the vector database.
     *
     * @param scripts list of {@link CodeScript} objects
     */
    @Override
    public void saveAll(List<CodeScript> scripts) {
        scripts.forEach(this::save);
    }

    /**
     * Executes k-NN similarity lookup using query embedding float array.
     *
     * @param queryEmbedding float array vector embedding of query
     * @return formatted Markdown string of matching code chunks and metadata
     */
    @Override
    public String lookup(float[] queryEmbedding) {
        int k = 5;
        StringBuilder builder = new StringBuilder();
        try (IndexReader reader = DirectoryReader.open(directory)) {
            Query query = new KnnFloatVectorQuery("embedding", queryEmbedding, k);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(query, k);
            StoredFields fields = searcher.storedFields();

            for (ScoreDoc doc : docs.scoreDocs) {
                Document retrievedDoc = fields.document(doc.doc);
                String path = retrievedDoc.get("path");
                String symbol = retrievedDoc.get("symbol");
                String type = retrievedDoc.get("type");
                String complexity = retrievedDoc.get("complexity");
                String content = retrievedDoc.get("content");

                builder.append("### Code Chunk: ")
                       .append(type != null && type.equals("method") ? "Method Symbol `" + symbol + "` in " : "File ")
                       .append(path).append("\n");
                if (complexity != null && !complexity.equals("0")) {
                    builder.append("**Cyclomatic Complexity:** ").append(complexity).append("\n");
                }
                builder.append("```java\n").append(content).append("\n```\n\n");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading vector database index directory: " + e.getMessage());
        } catch (IndexSearcher.TooManyClauses e) {
            System.err.println("[ERROR] Too many clauses for vector query: " + e.getMessage());
        }

        return builder.toString();
    }

    /**
     * Closes the underlying Lucene Directory and IndexWriter resources.
     */
    @Override
    public void closeDir() {
        try {
            if (writer != null && writer.isOpen()) {
                writer.close();
            }
            if (directory != null) {
                directory.close();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error closing vector database directory: " + e.getMessage());
        }
    }

    /**
     * Helper to open or create Lucene Directory at working directory path.
     *
     * @param workingDir path to vector DB index folder
     * @return initialized {@link Directory} instance
     */
    private static Directory getDirectory(String workingDir) {
        try {
            Path path = Path.of(workingDir);
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
            }
            return FSDirectory.open(path);
        } catch (IOException e) {
            System.err.println("[WARN] No codebase index found at " + workingDir + ". Did you forget to run 'cbase scan -s' first?");
            return null;
        }
    }

    /**
     * Helper to instantiate Lucene IndexWriter.
     *
     * @return initialized {@link IndexWriter}
     */
    private IndexWriter getWriter() {
        try {
            return new IndexWriter(directory, config);
        } catch (IOException e) {
            System.err.println("[ERROR] Error initializing IndexWriter for vector database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize IndexWriter", e);
        }
    }

    /**
     * Deletes all documents in the vector index.
     */
    private void deleteAllDocuments() {
        try {
            writer.deleteAll();
            writer.commit();
        } catch (IOException e) {
            System.err.println("[ERROR] Error deleting indexed embeddings: " + e.getMessage());
        }
    }

    /**
     * Helper to add a Lucene Document and commit changes.
     *
     * @param document Lucene Document to index
     */
    private void addDocument(Document document) {
        try {
            writer.addDocument(document);
            writer.commit();
        } catch (IOException e) {
            System.err.println("[ERROR] Error indexing document to vector database: " + e.getMessage());
        }
    }
}
