import com.backend.ai.adapters.EmbeddingsImpl;
import com.backend.ai.adapters.TokenizerImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TokenizerTest {
    @Test
    public void testTokenizer() {
        TokenizerImpl tokenizer = new TokenizerImpl();
        var token = tokenizer.tokenize("Hello");
        EmbeddingsImpl embeddings = new EmbeddingsImpl();
        System.out.println(Arrays.toString(embeddings.embedTokens(token)));
    }
}
