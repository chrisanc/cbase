package com.backend.cli.commands;

import com.backend.ai.adapters.VectorDBImpl;
import com.backend.ai.ports.VectorDB;
import com.backend.parser.adapters.ParserImpl;
import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;
import com.backend.parser.ports.Parser;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "scan", description = "Scan the whole project or certain files.",
        version = "v0.0.1 beta", footer = "Written by: Christian Sanchez. 2026.",
        mixinStandardHelpOptions = true
)
public class Scan implements Runnable {
    @Option(names = {"-s", "--save"}, description = "Saves the scanning results for future natural language queries")
    private boolean save;
    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        Parser parser = new ParserImpl();
        List<CodeScript> scripts = parser.readFileSystem();
        parser.parse(scripts);

        // Render rich complexity heatmap and metrics report
        String report = com.backend.cli.ui.ScanReportFormatter.generateReport(scripts);
        System.out.println(report);

        if (this.save) {
            System.out.println("💾 Indexing codebase vectors into local database...");
            VectorDB db = new VectorDBImpl();
            db.saveAll(scripts);
            db.closeDir();
            System.out.println("✅ Codebase vector index saved successfully to ./.cbase/db");
        }
    }
}
