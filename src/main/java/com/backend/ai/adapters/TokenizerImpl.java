package com.backend.ai.adapters;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.backend.ai.domain.Token;
import com.backend.ai.ports.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of the {@link Tokenizer} port using HuggingFace tokenizer models via DJL.
 */
public class TokenizerImpl implements Tokenizer {

    /**
     * Default constructor for TokenizerImpl.
     */
    public TokenizerImpl() {
    }

    /**
     * Tokenizes text content into a structured {@link Token} instance using HuggingFace models.
     *
     * @param content text string to tokenize
     * @param modelPath filesystem path to tokenizer model assets
     * @return tokenized {@link Token} representation
     * @throws RuntimeException if tokenization fails due to an I/O error
     */
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
            System.err.println("[ERROR] Could not get tokens from phrase: " + e.getMessage());
            throw new RuntimeException("Failed to tokenize content", e);
        }
    }

    /**
     * Decodes token IDs back into string text using HuggingFace tokenizer model.
     *
     * @param tokens token ID sequence to decode
     * @param modelPath filesystem path to tokenizer model assets
     * @return decoded text string, or empty string if decoding fails
     */
    @Override
    public String decode(long[] tokens, Path modelPath) {
        try (var tokenizer = HuggingFaceTokenizer.newInstance(modelPath)) {
            return tokenizer.decode(tokens);
        } catch (IOException e) {
            System.err.println("[ERROR] Error decoding tokens using model at: " + modelPath);
            return "";
        }
    }
}