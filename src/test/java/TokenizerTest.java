import com.backend.ai.adapters.EmbeddingsImpl;
import com.backend.ai.adapters.TokenizerImpl;
import com.backend.types.FilePath;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenizerTest {
    @Test
    public void testTokenizer() {
        TokenizerImpl tokenizer = new TokenizerImpl();
        var token = tokenizer.tokenize(
                "Hello World how are you doing im good ahsdjas aksdk ajksd",
                FilePath.LOCAL_CACHE.getValue("models", "minilm", "tokenizer.json")
        );
        EmbeddingsImpl embeddings = new EmbeddingsImpl();
        System.out.println(Arrays.toString(embeddings.embedTokens(token)));
    }
}
