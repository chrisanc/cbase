package com.backend.ai.ports;

import com.backend.ai.domain.Token;

import java.io.IOException;

public interface Tokenizer {
    Token tokenize(String line);
}
