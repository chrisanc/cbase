package com.backend.parser.adapters;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.ports.Parser;
import com.backend.parser.ports.SyntaxTree;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class ParserImpl implements Parser {
    // Save the current working directory
    private final String workingDir;
    // Define a list of directories to avoid (noise)
    private final HashSet<String> dirsToAvoid;
    // Define a regex pattern for valid scripts
    private final Pattern validScriptPattern;
    public ParserImpl() {
        this.workingDir = System.getProperty("user.dir");
        this.dirsToAvoid = new HashSet<>(Set.of(
                ".git", "build", ".gradle", "__pycache__", "__init__"
        ));
        this.validScriptPattern = Pattern.compile(".*\\.(java)");
    }

    /**
     * readFileSystem walks through the file system of the current directory
     * and extract the code of certain programming languages for its analysis.
     * */
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
            // Manage the dir files
            for (File f : Objects.requireNonNull(root.listFiles())) {
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
     * Parse method parses a file and extracts its information. Virtual Threads.
     * Uses AST implementations for this.
     * */
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
                System.err.println("A thread was interrupted...");
            }
        }
    }

    /**
     * Function used to extract the content of a script.
     * Using java.nio.Files readString method.
     * */
    private String readFileContent(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            System.err.println("Error reading the file...");
        }
        return "";
    }

    /**
     * Returns the SyntaxTree implementation to use.
     * Depends on the programming language the script was written in.
     * */
    private SyntaxTree getSyntaxTree(String ext) {
        return switch (ext) {
            case "java" -> JavaTreeImpl.getInstance();
            case "py" -> null;
            default -> null;
        };
    }
}
