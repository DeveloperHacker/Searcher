package analysers.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import javafx.util.Pair;

import java.util.Collections;
import java.util.List;

public class SimpleAstVisitor extends AbstractVisitor {

    @Override
    public void visit(CompilationUnit compilationUnit, Object arg) {
        super.visit(compilationUnit, arg);
    }

    @Override
    protected String getTypePackage(String name) {
        return null;
    }

    @Override
    protected Pair<String, List<String>> getFullClassName(Node node) {
        final Node parent = node.getParentNode();
        if (parent instanceof ClassOrInterfaceDeclaration) {
            return new Pair<>("", Collections.singletonList(((ClassOrInterfaceDeclaration) parent).getName()));
        } else {
            return getFullClassName(parent);
        }
    }
}
