package analysers;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.javatuples.Pair;

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
        if (parent instanceof TypeDeclaration) {
            return new Pair<>("", Collections.singletonList(((TypeDeclaration) parent).getName()));
        } else {
            return this.getFullClassName(parent);
        }
    }
}
