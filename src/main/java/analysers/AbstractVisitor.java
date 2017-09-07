package analysers;

import analysers.analysable.*;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javassist.NotFoundException;
import org.javatuples.Pair;
import utils.Sets;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractVisitor extends VoidVisitorAdapter<Object> {

    private Set<AstMethod> methods = new HashSet<>();

    @Override
    public void visit(ConstructorDeclaration declaration, Object arg) {
        final JavadocComment doc = declaration.getJavaDoc();
        final Pair<String, List<String>> pair = this.getFullClassName(declaration);
        final AsmClass owner = new AsmClass(new ArrayList<>(Arrays.asList(pair.getValue0().split("."))), pair.getValue1());
        final String name = declaration.getName();
        final List<Pair<AsmType, String>> parameters = declaration.getParameters().stream()
                .map(parameter -> new Pair<>(Parser.parseType(this.getTypeString(parameter.getType().toString())), parameter.getName()))
                .collect(Collectors.toList());
        final AsmType type = new AsmPrimitiveType("void");
        final String body = declaration.getBlock() == null ? "" : declaration.getBlock().toString();
        final MethodDescription description = new MethodDescription(name, owner, type, parameters);
        this.methods.add(new AstMethod(description, doc, body));
        super.visit(declaration, arg);
    }

    @Override
    public void visit(MethodDeclaration declaration, Object arg) {
        final JavadocComment doc = declaration.getJavaDoc();
        final Pair<String, List<String>> pair = this.getFullClassName(declaration);
        final AsmClass owner = new AsmClass(Arrays.asList(pair.getValue0().split(".")), pair.getValue1());
        final String name = declaration.getName();
        final List<Pair<AsmType, String>> parameters = declaration.getParameters().stream()
                .map(parameter -> new Pair<>(
                        Parser.parseType(this.getTypeString(parameter.getType().toString())),
                        parameter.getName()))
                .collect(Collectors.toList());
        final AsmType type = Parser.parseType(this.getTypeString(declaration.getType().toString()));
        final String body = declaration.getBody() == null ? "" : declaration.getBody().toString();
        final MethodDescription description = new MethodDescription(name, owner, type, parameters);
        this.methods.add(new AstMethod(description, doc, body));
        super.visit(declaration, arg);
    }

    protected abstract Pair<String, List<String>> getFullClassName(Node node);

    protected abstract String getTypePackage(String name);

    private String getTypeString(String fullName) {
        final String name = fullName.split("<")[0];
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

    Set<AstMethod> getMethods() {
        return this.methods;
    }

    public void setMethods(Set<AstMethod> methods) {
        this.methods = methods;
    }
}
