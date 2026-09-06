package com.backend.cli.ui;

public class ComplexityHeatmap {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";

    public static String getColorForComplexity(int complexity) {
        if (complexity < 5) {
            return GREEN;
        } else if (complexity < 10) {
            return YELLOW;
        } else {
            return RED + BOLD;
        }
    }

    public static String getBadge(int complexity) {
        if (complexity < 5) {
            return GREEN + "[LOW]" + RESET;
        } else if (complexity < 10) {
            return YELLOW + "[MED]" + RESET;
        } else {
            return RED + BOLD + "[HIGH RISK]" + RESET;
        }
    }

    public static String formatHeatmapBar(int complexity, int maxVal) {
        int totalBlocks = 15;
        int filled = Math.min(totalBlocks, (int) Math.round(((double) complexity / Math.max(15, maxVal)) * totalBlocks));
        filled = Math.max(1, filled);

        String color = getColorForComplexity(complexity);
        StringBuilder sb = new StringBuilder();
        sb.append(color).append("[");
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        for (int i = filled; i < totalBlocks; i++) {
            sb.append("░");
        }
        sb.append("] ").append(complexity).append(RESET);
        return sb.toString();
    }
}
