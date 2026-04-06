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
        if (!this.save) {
            this.printScripts(scripts);
        } else {
            VectorDB db = new VectorDBImpl();
            db.saveAll(scripts);
            db.closeDir();
        }
    }

    /**
     * Method used to give feedback to the user about possible dangerous scripts
     * based on metrics such as the cyclical complexity
     * */
    private void printScripts(List<CodeScript> scripts) {
        for (CodeScript script : scripts) {
            for (Method method : script.getMethods()) {
                if (method.getCyclicalComplexity() < 10) continue;

                System.out.println("POTENTIAL PROBLEM!");
                System.out.printf("* At %s:%s\n", script.getPath(), method.getName());
                System.out.println("  The method may be too complex.\n");
            }
        }
    }
}
