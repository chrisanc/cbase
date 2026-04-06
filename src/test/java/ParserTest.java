import com.backend.parser.adapters.JavaTreeImpl;
import com.backend.parser.domain.CodeScript;
import org.junit.jupiter.api.Test;

public class ParserTest {
    @Test
    public void testParser() {
        CodeScript script = new CodeScript("root", "script",
                "class Public { public void test() { int i = 0; switch(i) { case 0: i = 1;\ncase 1: i = 1;\ndefault: i = 100; }; } }"
        );
        JavaTreeImpl impl = JavaTreeImpl.getInstance();
        impl.analyze(script);
        System.out.println(script.toString());

        /* VectorDBImpl db = new VectorDBImpl();
           db.save(script);*/
    }
}
