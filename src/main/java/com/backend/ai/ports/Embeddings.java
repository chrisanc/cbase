package com.backend.ai.ports;

import java.util.List;

public interface Embeddings {
    List<Double> embedText(String text);
}
