package com.backend.ai.adapters;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import com.backend.ai.ports.Embeddings;
import com.backend.ai.ports.Tokenizer;
import com.backend.ai.ports.VectorDB;
import com.backend.parser.domain.CodeScript;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class VectorDBImpl implements VectorDB {
    private final Directory directory = getDirectory(System.getProperty("user.dir") + "/.sentinel/db");
    private final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    private final IndexWriter writer = getWriter();

    private final Tokenizer tokenizer = new TokenizerImpl();
    private final Embeddings embeddings = new EmbeddingsImpl();

    @Override
    public void save(CodeScript script) {
        Document doc = new Document();
        doc.add(
            new KnnFloatVectorField(
                script.getPath(), embeddings.embedTokens(tokenizer.tokenize(script.getContent()))
            )
        );
        this.addDocument(doc);
    }

    @Override
    public void saveAll(List<CodeScript> scripts) {
        scripts.forEach(this::save);
    }

    @Override
    public void lookup(String path) {}

    private static Directory getDirectory(String workingDir) {
        Directory dir = null;
        try {
            dir = FSDirectory.open(Path.of(workingDir));
        } catch (IOException e) {
            System.err.println("Not a .sentinel found in this workdir. Forgot to run 'sentinel init'?");
            System.exit(1);
        }

        return dir;
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
