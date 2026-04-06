package com.backend.cli.commands;

import picocli.CommandLine.Command;

@Command(
        name = "scan", description = "Scan the whole project or certain files.",
        version = "v0.0.1 beta", footer = "Written by: Christian Sanchez. 2026."
)
public class Scan implements Runnable {
    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        System.out.println("Scanning!!");
    }
}
