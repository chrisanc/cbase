import com.backend.parser.adapters.JavaTreeImpl;
import com.backend.parser.domain.CodeScript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    @Test
    public void testParserAndMethodChunking() {
        CodeScript script = new CodeScript("src/Test.java", "Test.java",
                "class Public {  public void test() { if (true) System.out.println(\"Hola\"); } }"
        );
        JavaTreeImpl impl = JavaTreeImpl.getInstance();
        impl.analyze(script);
        
        assertNotNull(script.getMethods());
        assertEquals(1, script.getMethods().size());
        
        var method = script.getMethods().get(0);
        assertEquals("test", method.getName());
        assertEquals(2, method.getCyclicalComplexity());

        String chunk = method.toChunkText(script.getPath());
        assertTrue(chunk.contains("// File: src/Test.java"));
        assertTrue(chunk.contains("// Method: test"));
        assertTrue(chunk.contains("// Cyclomatic Complexity: 2"));
    }
}
