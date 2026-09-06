package com.backend.cli.commands;

import com.backend.ai.adapters.VectorDBImpl;
import com.backend.ai.ports.VectorDB;
import com.backend.parser.adapters.ParserImpl;
import com.backend.parser.domain.CodeScript;
import com.backend.parser.ports.Parser;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.util.List;

/**
 * Picocli command class for scanning codebase scripts, computing cyclomatic complexity metrics,
 * rendering CLI heatmap reports, and persisting vector embeddings.
 */
@Command(
        name = "scan", description = "Scan project files to calculate cyclomatic complexity and generate vector embeddings.",
        version = "v1.0.0", mixinStandardHelpOptions = true
)
public class Scan implements Runnable {
    @Option(names = {"-s", "--save"}, description = "Persists scanning vector results for natural language querying")
    private boolean save;

    /**
     * Executes project filesystem analysis, complexity report generation, and vector indexing.
     */
    @Override
    public void run() {
        Parser parser = new ParserImpl();
        List<CodeScript> scripts = parser.readFileSystem();
        parser.parse(scripts);

        String report = com.backend.cli.ui.ScanReportFormatter.generateReport(scripts);
        System.out.println(report);

        if (this.save) {
            System.out.println("[INFO] Indexing codebase vectors into local database...");
            VectorDB db = new VectorDBImpl();
            db.saveAll(scripts);
            db.closeDir();
            System.out.println("[SUCCESS] Codebase vector index saved successfully to ./.cbase/db");
        }
    }
}
