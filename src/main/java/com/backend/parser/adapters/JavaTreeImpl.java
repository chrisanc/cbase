package com.backend.parser.adapters;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;
import com.backend.parser.ports.SyntaxTree;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaTreeImpl implements SyntaxTree {
    private static JavaTreeImpl instance;
    private JavaTreeImpl() {}

    public static JavaTreeImpl getInstance() {
        if (instance == null) instance = new JavaTreeImpl();
        return instance;
    }

    @Override
    public void analyze(CodeScript script) {
        // Execute the AST parsing on the code (Java)
        CompilationUnit unit = StaticJavaParser.parse(script.getContent());
        // Extract the methods information
        List<Method> methods = unit.findAll(MethodDeclaration.class).stream().map(
                method -> {
                    return new Method(
                            method.getName().asString(),
                            method.getParameters(),
                            method.getBody().isPresent() ? method.getBody().get() : null
                    );
                }
        ).toList();

        // Iterate the methods to get the analytics per method
        for (Method method : methods) {
            // Calculate the cyclical complexity
            this.calculateCyclicalComplexity(method);
        }

        // Link the methods to the script
        script.setMethods(methods);
    }

    @Override
    public void calculateCyclicalComplexity(Method method) {
        AtomicInteger complexity = new AtomicInteger(1);
        // Define a hashset (for O(1) lookup) with the classes +1
        final var classes = new HashSet<>(Set.of(
                BinaryExpr.class,
                ForStmt.class,
                IfStmt.class,
                TryStmt.class,
                ForEachStmt.class,
                SwitchEntry.class,
                WhileStmt.class
        ));
        // Walk through the method body and calculate the complexity
        method.getBody().walk(
            node -> {
                // Get the node class
                var clazz = node.getClass();
                // If the classes set contains the class
                if (classes.contains(clazz)) complexity.set(complexity.get() + 1);
            }
        );
        // Return the final complexity
        method.setCyclicalComplexity(complexity.get());
    }
}
