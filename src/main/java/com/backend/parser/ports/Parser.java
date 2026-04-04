package com.backend.parser.ports;

import com.backend.parser.domain.CodeScript;

import java.util.List;

public interface Parser {
    List<CodeScript> readFileSystem();
    void parse(List<CodeScript> scripts);
}
