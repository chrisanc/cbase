package com.backend.ai.adapters;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;

public class TokenizerImpl implements Tokenizer {
    @Override
    public Token tokenize(String line, String path) {
        try (var tokenizer = HuggingFaceTokenizer.newInstance(Path.of(path))) {
            Encoding encode = tokenizer.encode(line);
            return Token
                .builder()
                .ids(encode.getIds())
                .attentionMask(encode.getAttentionMask())
                .typeIds(encode.getTypeIds())
                .build();
        } catch (IOException e) {
            System.err.println("Couldn't get the tokens from the phrase '" + line + "'");
            System.exit(1);
        }

        return null;
    }
}
