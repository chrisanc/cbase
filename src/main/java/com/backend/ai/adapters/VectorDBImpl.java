package com.backend.ai.adapters;

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

public class VectorDBImpl implements VectorDB {
    private final Directory directory = getDirectory(System.getProperty("user.dir") + "/.sentinel/db");
    private final IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    private final IndexWriter writer = getWriter();

    @Override
    public void save(CodeScript script) {
        Document doc = new Document();
        // Add the embedding to the document
        doc.add(new KnnFloatVectorField(script.getPath(), new float[]{1.2f, 3.4f}));
        this.addDocument(doc);

        this.closeDirectory(directory);
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

    private void closeDirectory(Directory dir) {
        try {
            dir.close();
        } catch (IOException e) {
            System.err.println("Error closing the directory opened...");
            System.exit(1);
        }
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
