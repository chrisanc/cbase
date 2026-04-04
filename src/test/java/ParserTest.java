import com.backend.parser.adapters.ParserImpl;
import com.backend.parser.domain.CodeScript;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ParserTest {
    @Test
    public void testParser() {
        ParserImpl parser = new ParserImpl();
        List<CodeScript> scripts = parser.readFileSystem();
        parser.parse(scripts);
    }
}
