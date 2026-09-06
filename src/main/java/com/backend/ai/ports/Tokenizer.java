package com.backend.ai.ports;

import com.backend.ai.domain.Token;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Port interface for tokenizing string content into token objects and decoding token IDs back to text.
 */
public interface Tokenizer {
    /**
     * Encodes text content into structured token representation.
     *
     * @param content text string to tokenize
     * @param modelPath filesystem path to the tokenizer configuration model file
     * @return {@link Token} object containing token IDs and attention masks
     */
    Token tokenize(String content, Path modelPath);

    /**
     * Decodes token ID sequence back into text string.
     *
     * @param tokens array of token IDs to decode
     * @param modelPath filesystem path to the tokenizer configuration model file
     * @return decoded text string
     */
    String decode(long[] tokens, Path modelPath);
}