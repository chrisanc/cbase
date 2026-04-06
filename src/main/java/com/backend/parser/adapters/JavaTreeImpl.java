package com.backend.parser.adapters;

import com.backend.parser.domain.CodeScript;
import com.backend.parser.domain.Method;
import com.backend.parser.ports.SyntaxTree;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JavaTreeImpl implements SyntaxTree {
    private static JavaTreeImpl instance;
    private JavaTreeImpl() {}

    public static JavaTreeImpl getInstance() {
        if (instance == null) instance = new JavaTreeImpl();
        return instance;
    }

    @Override
    public void analyze(CodeScript script) {
        // Configure Java 21 parsing
        StaticJavaParser.setConfiguration(
                new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
        );
        // Execute the AST parsing on the code (Java)
        CompilationUnit unit = StaticJavaParser.parse(script.getContent());
        // Extract the methods information
        List<Method> methods = unit.findAll(MethodDeclaration.class).stream().map(
                method -> {
                    return new Method(
                            method.getName().asString(),
                            method.getParameters(),
                            method.getBody().orElse(null)
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
        if (method.getBody() == null) return;

        int complexity = 1;
        final var decisionStmts = new HashSet<>(Set.of(
                ForStmt.class,
                ForEachStmt.class,
                WhileStmt.class,
                IfStmt.class
        ));
        // Define a hashset (for O(1) lookup) with the classes +1
        final var classes = new HashSet<>(Set.of(
                BinaryExpr.class,
                TryStmt.class,
                SwitchEntry.class
        ));
        // Add all classes together
        classes.addAll(decisionStmts);
        // Walk through the method body and calculate the complexity
        for (Iterator<Node> it = method.getBody().stream().iterator(); it.hasNext(); ) {
            Node node = it.next();
            Class<? extends Node> nodeClass = node.getClass();

            if (!classes.contains(nodeClass)) continue;

            if (nodeClass == BinaryExpr.class) {
                if (
                        node.getParentNode().isEmpty() ||
                        !decisionStmts.contains(node.getParentNode().get().getClass())
                ) continue;

                BinaryExpr.Operator op = ((BinaryExpr) node).getOperator();
                switch (op) {
                    case BinaryExpr.Operator.OR, BinaryExpr.Operator.AND:
                        complexity++;
                }
            } else {
                complexity++;
            }
        }
        // Return the final complexity
        method.setCyclicalComplexity(complexity);
    }
}
