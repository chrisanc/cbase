import com.backend.ai.adapters.VectorDBImpl;
import com.backend.parser.adapters.ParserImpl;
import com.backend.parser.adapters.JavaTreeImpl;
import com.backend.parser.domain.CodeScript;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ParserTest {
    @Test
    public void testParser() {
        ParserImpl parser = new ParserImpl();
        List<CodeScript> scripts = parser.readFileSystem();
        JavaTreeImpl impl = JavaTreeImpl.getInstance();
        impl.analyze(scripts.getFirst());

        VectorDBImpl db = new VectorDBImpl();
        db.save(scripts.getFirst());
    }
}
