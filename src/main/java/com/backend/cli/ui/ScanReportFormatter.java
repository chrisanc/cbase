package com.backend.cli.ui;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatter class responsible for compiling codebase metrics and generating
 * professional CLI analysis reports for scanned projects.
 */
public class ScanReportFormatter {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ScanReportFormatter() {
    }

    /**
     * Value object representing a high-complexity method hotspot within a project script.
     */
    public static class Hotspot {
        private final String path;
        private final String methodName;
        private final int complexity;

        /**
         * Constructs a new Hotspot instance.
         *
         * @param path the file path containing the method
         * @param methodName the name of the method
         * @param complexity the cyclomatic complexity score
         */
        public Hotspot(String path, String methodName, int complexity) {
            this.path = path;
            this.methodName = methodName;
            this.complexity = complexity;
        }

        /**
         * @return the file path of the hotspot
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the method name of the hotspot
         */
        public String getMethodName() {
            return methodName;
        }

        /**
         * @return the cyclomatic complexity score of the hotspot
         */
        public int getComplexity() {
            return complexity;
        }
    }

    /**
     * Generates a complete, professional CLI report detailing scanned files, total methods,
     * complexity statistics, and high-risk code hotspots.
     *
     * @param scripts list of parsed CodeScript objects
     * @return a formatted multi-line report string suitable for terminal display
     */
    public static String generateReport(List<CodeScript> scripts) {
        int totalFiles = scripts.size();
        int totalMethods = 0;
        int maxComplexity = 0;
        double sumComplexity = 0;
        int lowCount = 0;
        int medCount = 0;
        int highCount = 0;

        List<Hotspot> hotspots = new ArrayList<>();

        for (CodeScript script : scripts) {
            if (script.getMethods() == null) continue;
            for (Method method : script.getMethods()) {
                totalMethods++;
                int comp = method.getCyclicalComplexity();
                sumComplexity += comp;

                if (comp > maxComplexity) {
                    maxComplexity = comp;
                }

                if (comp < 5) {
                    lowCount++;
                } else if (comp < 10) {
                    medCount++;
                } else {
                    highCount++;
                    hotspots.add(new Hotspot(script.getPath(), method.getName(), comp));
                }
            }
        }

        double avgComplexity = totalMethods > 0 ? sumComplexity / totalMethods : 0;
        StringBuilder sb = new StringBuilder();

        sb.append(ComplexityHeatmap.CYAN).append(ComplexityHeatmap.BOLD)
          .append("\n================================================================================\n")
          .append("CBASE CODEBASE METRICS & CYCLOMATIC COMPLEXITY REPORT\n")
          .append("================================================================================\n")
          .append(ComplexityHeatmap.RESET);

        sb.append(String.format("Files Scanned:       %d\n", totalFiles));
        sb.append(String.format("Total Methods:       %d\n", totalMethods));
        sb.append(String.format("Max Complexity:      %d\n", maxComplexity));
        sb.append(String.format("Avg Complexity:      %.2f\n", avgComplexity));
        sb.append("--------------------------------------------------------------------------------\n");

        double lowPct = totalMethods > 0 ? (lowCount * 100.0) / totalMethods : 0;
        double medPct = totalMethods > 0 ? (medCount * 100.0) / totalMethods : 0;
        double highPct = totalMethods > 0 ? (highCount * 100.0) / totalMethods : 0;

        sb.append(String.format("%s Low Complexity  (<5):  %3d methods  (%5.1f%%)%s\n",
                ComplexityHeatmap.GREEN + "[LOW]" + ComplexityHeatmap.RESET, lowCount, lowPct, ComplexityHeatmap.RESET));
        sb.append(String.format("%s Med Complexity (5-9):   %3d methods  (%5.1f%%)%s\n",
                ComplexityHeatmap.YELLOW + "[MED]" + ComplexityHeatmap.RESET, medCount, medPct, ComplexityHeatmap.RESET));
        sb.append(String.format("%s High Risk     (>=10):   %3d methods  (%5.1f%%)%s\n",
                ComplexityHeatmap.RED + "[HIGH]" + ComplexityHeatmap.RESET, highCount, highPct, ComplexityHeatmap.RESET));
        sb.append("--------------------------------------------------------------------------------\n");

        if (hotspots.isEmpty()) {
            sb.append(ComplexityHeatmap.GREEN + "[SUCCESS] No high-risk cyclomatic complexity hotspots detected.\n" + ComplexityHeatmap.RESET);
        } else {
            sb.append(ComplexityHeatmap.RED + ComplexityHeatmap.BOLD + "[WARN] HIGH COMPLEXITY HOTSPOTS & REFACTORING RECOMMENDATIONS:\n" + ComplexityHeatmap.RESET);
            for (Hotspot h : hotspots) {
                String badge = ComplexityHeatmap.getBadge(h.getComplexity());
                String bar = ComplexityHeatmap.formatHeatmapBar(h.getComplexity(), maxComplexity);
                sb.append(String.format("  * %s %s:%s\n    Heatmap: %s\n",
                        badge, h.getPath(), h.getMethodName(), bar));
            }
        }

        sb.append(ComplexityHeatmap.CYAN).append(ComplexityHeatmap.BOLD)
          .append("================================================================================\n\n")
          .append(ComplexityHeatmap.RESET);

        return sb.toString();
    }
}
