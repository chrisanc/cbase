package com.backend.ai.ports;

public interface LLM <I, O> {
    O predict(I input);
}
