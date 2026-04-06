package com.backend;

import com.backend.cli.commands.Scan;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "cbase", description = "Let CBase walk you through your project :D.",
        version = "v0.0.1 beta", footer = "Written by: Christian Sanchez. 2026.",
        subcommands = {Scan.class}, mixinStandardHelpOptions = true
)
public class CBase implements Runnable {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CBase()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        System.out.println("Welcome to CBase: a CLI tool to help you understand big programming projects.");
        System.out.println("Explore the available commands within the tool using the flags -h or --help.");
        System.out.println("For any issue, report it on the official github page:");
        System.out.println("Written by: Christian Sanchez. cdavidsanchez054@gmail.com");
    }
}