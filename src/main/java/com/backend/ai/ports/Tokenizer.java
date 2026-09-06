package com.backend.ai.ports;

import com.backend.ai.domain.Token;

import java.io.IOException;
import java.nio.file.Path;

public interface Tokenizer {
    Token tokenize(String content, Path modelPath);
    String decode(long[] tokens, Path modelPath);
}