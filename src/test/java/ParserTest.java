import com.backend.parser.adapters.JavaTreeImpl;
import com.backend.parser.domain.CodeScript;
import org.junit.jupiter.api.Test;

public class ParserTest {
    @Test
    public void testParser() {
        CodeScript script = new CodeScript("root", "script",
                "class Public {  public void test() { if (true) System.out.println(\"Hola\"); } }"
        );
        JavaTreeImpl impl = JavaTreeImpl.getInstance();
        impl.analyze(script);
        System.out.println(script.toString());

        /* VectorDBImpl db = new VectorDBImpl();
           db.save(script);*/
    }
}
