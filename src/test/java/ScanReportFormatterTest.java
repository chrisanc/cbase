import com.backend.cli.ui.ComplexityHeatmap;
import com.backend.cli.ui.ScanReportFormatter;
import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;
import com.github.javaparser.ast.NodeList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScanReportFormatterTest {

    @Test
    public void testComplexityHeatmapColors() {
        assertEquals(ComplexityHeatmap.GREEN, ComplexityHeatmap.getColorForComplexity(2));
        assertEquals(ComplexityHeatmap.YELLOW, ComplexityHeatmap.getColorForComplexity(7));
        assertEquals(ComplexityHeatmap.RED + ComplexityHeatmap.BOLD, ComplexityHeatmap.getColorForComplexity(15));
    }

    @Test
    public void testScanReportFormatterWithHotspots() {
        CodeScript script = new CodeScript("src/Sample.java", "Sample.java", "code");
        Method simpleMethod = new Method("simple", new NodeList<>(), null);
        simpleMethod.setCyclicalComplexity(2);

        Method complexMethod = new Method("complex", new NodeList<>(), null);
        complexMethod.setCyclicalComplexity(12);

        script.setMethods(List.of(simpleMethod, complexMethod));

        String report = ScanReportFormatter.generateReport(List.of(script));

        assertNotNull(report);
        assertTrue(report.contains("Files Scanned:       1"));
        assertTrue(report.contains("Total Methods:       2"));
        assertTrue(report.contains("Max Complexity:      12"));
        assertTrue(report.contains("COMPLEXITY HOTSPOTS & REFACTORING RECOMMENDATIONS:"));
        assertTrue(report.contains("src/Sample.java:complex"));
    }

    @Test
    public void testScanReportFormatterNoHotspots() {
        CodeScript script = new CodeScript("src/Clean.java", "Clean.java", "code");
        Method simpleMethod = new Method("cleanMethod", new NodeList<>(), null);
        simpleMethod.setCyclicalComplexity(3);

        script.setMethods(List.of(simpleMethod));

        String report = ScanReportFormatter.generateReport(List.of(script));

        assertNotNull(report);
        assertTrue(report.contains("No high-risk cyclomatic complexity hotspots detected"));
    }
}
