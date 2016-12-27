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
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javassist.NotFoundException;
import org.javatuples.Pair;
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
        final String ownerFullName = "L" + (pair.getValue0().length() > 0 ? pair.getValue0() + "." : "") + String.join("$", pair.getValue1()) + ";";
        final AsmClass owner = Parser.parseClass(ownerFullName);
        final String name = declaration.getName();
        final List<Pair<AsmType, String>> parameters = declaration.getParameters().stream()
                .map(parameter -> new Pair<>(Parser.parseType(this.getTypeString(parameter.getType().toString())), parameter.getName()))
                .collect(Collectors.toList());
        final AsmType type = Parser.parseType(getTypeString(declaration.getType().toString()));
        final String body = declaration.getBody() == null ? "" : declaration.getBody().toString();
        final MethodDescription method = new MethodDescription(name, owner, type, parameters);
        this.methods.add(new AstMethod(method, new MethodParts(doc, body)));
        super.visit(declaration, arg);
    }

    protected abstract Pair<String, List<String>> getFullClassName(Node node);

    protected abstract String getTypePackage(String name);

    private String getTypeString(String fullName) {
        final String name = fullName.split("\\<")[0];
        final String generic = fullName.substring(name.length());
        final String[] arrName = name.replace('.', '$').split("$");
        final String pkg = this.getTypePackage(arrName[arrName.length - 1]);
        if (pkg == null) {
            if (AsmPrimitiveType.isPrimitive(fullName)) {
                return AsmPrimitiveType.shortRepresentation(fullName).toString();
            }
            return String.format("L%s%s;", name, this.getGenericString(generic));
        }
        return String.format("L%s.%s%s;", pkg, name, this.getGenericString(generic));
    }

    private String getGenericString(String generic) {
        if (generic.length() == 0) return "";
        List<String> generics = Parser.parseGenerics(generic).stream()
                .map(this::getTypeString)
                .collect(Collectors.toList());
        return String.format("<%s>", String.join(", ", generics));
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
