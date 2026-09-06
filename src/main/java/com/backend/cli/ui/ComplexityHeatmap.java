package com.backend.cli.ui;

/**
 * Utility class providing ANSI escape codes and formatting logic
 * for cyclomatic complexity metrics and heatmap progress bars.
 */
public class ComplexityHeatmap {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";

    /**
     * Resolves the appropriate ANSI color string based on the given complexity value.
     *
     * @param complexity the cyclomatic complexity score
     * @return the ANSI color code string
     */
    public static String getColorForComplexity(int complexity) {
        if (complexity < 5) {
            return GREEN;
        } else if (complexity < 10) {
            return YELLOW;
        } else {
            return RED + BOLD;
        }
    }

    /**
     * Generates a formatted badge string reflecting the risk level of the given complexity.
     *
     * @param complexity the cyclomatic complexity score
     * @return a colorized text badge string
     */
    public static String getBadge(int complexity) {
        if (complexity < 5) {
            return GREEN + "[LOW]" + RESET;
        } else if (complexity < 10) {
            return YELLOW + "[MED]" + RESET;
        } else {
            return RED + BOLD + "[HIGH RISK]" + RESET;
        }
    }

    /**
     * Formats an ASCII heatmap bar representing relative cyclomatic complexity.
     *
     * @param complexity the cyclomatic complexity score
     * @param maxVal the maximum complexity value in the scanned set
     * @return a formatted ANSI string containing the progress bar and numerical score
     */
    public static String formatHeatmapBar(int complexity, int maxVal) {
        int totalBlocks = 15;
        int filled = Math.min(totalBlocks, (int) Math.round(((double) complexity / Math.max(15, maxVal)) * totalBlocks));
        filled = Math.max(1, filled);

        String color = getColorForComplexity(complexity);
        StringBuilder sb = new StringBuilder();
        sb.append(color).append("[");
        for (int i = 0; i < filled; i++) {
            sb.append("=");
        }
        for (int i = filled; i < totalBlocks; i++) {
            sb.append("-");
        }
        sb.append("] ").append(complexity).append(RESET);
        return sb.toString();
    }
}
