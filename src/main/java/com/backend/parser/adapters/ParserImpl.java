package com.backend.parser.adapters;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.ports.Parser;
import com.backend.parser.ports.SyntaxTree;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of the {@link Parser} port for traversing source code repositories,
 * filtering unwanted directories, reading file contents, and parallelizing AST parsing via Java 21 Virtual Threads.
 */
public final class ParserImpl implements Parser {
    // Save the current working directory
    private final String workingDir;
    // Define a list of directories to avoid (noise)
    private final HashSet<String> dirsToAvoid;
    // Define a regex pattern for valid scripts
    private final Pattern validScriptPattern;

    /**
     * Default constructor initializing working directory, default directory exclusions, and Java file matching pattern.
     */
    public ParserImpl() {
        this.workingDir = System.getProperty("user.dir");
        this.dirsToAvoid = new HashSet<>(Set.of(
                ".git", "build", ".gradle", "__pycache__", "__init__"
        ));
        this.validScriptPattern = Pattern.compile(".*\\.(java)");
    }

    /**
     * Traverses the project filesystem using depth-first search (DFS) to collect supported source files.
     *
     * @return list of discovered {@link CodeScript} instances
     */
    @Override
    public List<CodeScript> readFileSystem() {
        // Get the path of the working dir
        File root = new File(this.workingDir);
        // Define a stack that'll be saving only directories
        Stack<File> stack = new Stack<>();
        // Create a list to save valid scripts
        List<CodeScript> scripts = new ArrayList<>();
        stack.push(root);

        // DFS implementation on the file system
        while (!stack.empty()) {
            root = stack.pop();
            File[] files = root.listFiles();
            if (files == null) continue;

            // Manage the dir files
            for (File f : files) {
                // Manage directories
                if (f.isDirectory()) {
                    if (!this.dirsToAvoid.contains(f.getName())) stack.push(f);
                    continue;
                }
                // Manage the files
                if (this.validScriptPattern.matcher(f.getName()).matches()) {
                    scripts.add(
                            new CodeScript(f.getPath(), f.getName(), this.readFileContent(f.getPath()))
                    );
                }
            }
        }

        return scripts;
    }

    /**
     * Parses the AST for each script concurrently using Java 21 Virtual Threads.
     *
     * @param scripts list of {@link CodeScript} objects to process
     */
    @Override
    public void parse(List<CodeScript> scripts) {
        List<Thread> threads = new ArrayList<>();
        for (CodeScript script : scripts) {
            threads.add(Thread.ofVirtual().start(
                () -> {
                    SyntaxTree tree = this.getSyntaxTree("java");
                    if (tree != null) tree.analyze(script);
                }
            ));
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("[WARN] Parsing thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Helper method to read the raw text content of a file using {@link Files#readString(Path)}.
     *
     * @param path target file path string
     * @return raw content string, or empty string if reading fails
     */
    private String readFileContent(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading file content at " + path + ": " + e.getMessage());
        }
        return "";
    }

    /**
     * Resolves the appropriate {@link SyntaxTree} implementation according to file extension.
     *
     * @param ext file extension identifier string
     * @return matching {@link SyntaxTree} implementation, or null if unsupported
     */
    private SyntaxTree getSyntaxTree(String ext) {
        return switch (ext) {
            case "java" -> JavaTreeImpl.getInstance();
            case "py" -> null;
            default -> null;
        };
    }
}
