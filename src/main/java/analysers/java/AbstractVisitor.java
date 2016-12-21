package analysers.java;

import analysers.AstMethod;
import analysers.MethodDescription;
import analysers.MethodParts;
import analysers.Parser;
import analysers.bytecode.AsmClass;
import analysers.bytecode.AsmPrimitiveType;
import analysers.bytecode.AsmType;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javafx.util.Pair;
import javassist.NotFoundException;
import utils.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractVisitor extends VoidVisitorAdapter<Object> {

    private Set<AstMethod> methods = new HashSet<>();

    @Override
    public void visit(MethodDeclaration declaration, Object arg) {
        final JavadocComment doc = declaration.getJavaDoc();
        final Pair<String, List<String>> pair = getFullClassName(declaration);
        final String ownerFullName = "L" + (pair.getKey().length() > 0 ? pair.getKey() + "." : "") + String.join("$", pair.getValue()) + ";";
        final AsmClass owner = Parser.parseClass(ownerFullName);
        final String name = declaration.getName();
        final List<AsmType> parameters = declaration.getParameters().stream()
                .map(Parameter::getType)
                .map(this::getTypeString)
                .map(Parser::parseType)
                .collect(Collectors.toList());
        final AsmType type = Parser.parseType(getTypeString(declaration.getType()));
        final String body = declaration.getBody() == null ? "" : declaration.getBody().toString();
        final MethodDescription method = new MethodDescription(name, owner, type, parameters);
        this.methods.add(new AstMethod(method, new MethodParts(doc, body)));
        super.visit(declaration, arg);
    }

    protected abstract Pair<String, List<String>> getFullClassName(Node node);

    protected abstract String getTypePackage(String name);

    private String getTypeString(Type type) {
        final String fullName = type.toString().replace('.', '$');
        final String[] arrName = fullName.split("\\$");
        final String name = arrName[0];
        final String pkg = this.getTypePackage(name);
        if (pkg == null) {
            if(AsmPrimitiveType.isPrimitive(name)) {
                return AsmPrimitiveType.shortRepresentation(name).toString();
            }
            return String.format("L%s;", arrName[arrName.length - 1]);
        }
        return String.format("L%s.%s;", pkg, fullName);
    }

    public AstMethod getMethod(MethodDescription description) throws NotFoundException {
        return Sets.getElement(this.methods, description);
    }

    public Set<AstMethod> getMethods() {
        return this.methods;
    }

    public void setMethods(Set<AstMethod> methods) {
        this.methods = methods;
    }
}
