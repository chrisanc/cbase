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

public class VectorDBImpl implements VectorDB {
    private final Directory directory = getDirectory(FilePath.WDIR_CACHE.getValue("db").toString());
    private final Analyzer analyzer = new StandardAnalyzer();
    private final IndexWriterConfig config = new IndexWriterConfig(analyzer);
    private final IndexWriter writer = getWriter();

    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();

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

    @Override
    public void saveAll(List<CodeScript> scripts) {
        scripts.forEach(this::save);
    }

    @Override
    public String lookup(float[] queryEmbedding) {
        int k = 5;
        StringBuilder builder = new StringBuilder();
        try {
            Query query = new KnnFloatVectorQuery("embedding", queryEmbedding, k);
            IndexReader reader = DirectoryReader.open(directory);
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

            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading vector database index directory.");
        } catch (IndexSearcher.TooManyClauses e) {
            System.err.println("Too many clauses for vector query.");
        }

        return builder.toString();
    }

    @Override
    public void closeDir() {
        try {
            directory.close();
        } catch (IOException e) {
            System.err.println("Error closing the directory opened...");
            System.exit(1);
        }
    }

    private static Directory getDirectory(String workingDir) {
        Directory dir = null;
        try {
            Path path = Path.of(workingDir);
            if (!java.nio.file.Files.exists(path)) {
                java.nio.file.Files.createDirectories(path);
            }
            dir = FSDirectory.open(path);
        } catch (IOException e) {
            System.err.println("No codebase index found at " + workingDir + ". Did you forget to run 'cbase scan -s' first?");
        }

        return dir;
    }

    private IndexWriter getWriter() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, config);
            return writer;
        } catch (IOException e) {
            System.err.println("Error getting the index writer for database purposes... Try again later.");
            System.exit(1);
        }

        return writer;
    }

    private void deleteAllDocuments() {
        try {
            // Create the query
            writer.deleteAll();
            // Confirm the changes
            writer.commit();
        } catch (IOException e) {
            System.err.println("Error deleting all the indexed embeddings...");
            System.exit(1);
        }
    }

    private void addDocument(Document document) {
        try {
            writer.addDocument(document);
            writer.commit();
        } catch (IOException e) {
            System.err.println("Error indexing a document to the database.");
        }
    }
}
