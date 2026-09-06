package com.backend.ai.adapters;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;

public class TokenizerImpl implements Tokenizer {
    @Override
    public Token tokenize(String content, Path modelPath) {
        try (var tokenizer = HuggingFaceTokenizer.newInstance(modelPath)) {
            Encoding encode = tokenizer.encode(content);
            return Token
                    .builder()
                    .ids(encode.getIds())
                    .attentionMask(encode.getAttentionMask())
                    .typeIds(encode.getTypeIds())
                    .build();
        } catch (IOException e) {
            System.err.println("Couldn't get the tokens from the phrase '" + content + "'");
            System.exit(1);
        }

        return null;
    }

    @Override
    public String decode(long[] tokens, Path modelPath) {
        try (var tokenizer = HuggingFaceTokenizer.newInstance(modelPath)) {
            return tokenizer.decode(tokens);
        } catch (IOException e) {
            System.err.println("Error decoding tokens using model at: " + modelPath);
            return "";
        }
    }
}