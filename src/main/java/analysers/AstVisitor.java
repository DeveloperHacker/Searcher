package analysers;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import javafx.util.Pair;
import org.reflections.Reflections;
import parts.AstClass;
import parts.AstMethod;
import parts.AstPrimitiveType;
import parts.AstType;

import java.util.*;
import java.util.stream.Collectors;

public class AstVisitor extends VoidVisitorAdapter<Object> {

    private String pkg;
    private Map<String, String> imports;

    @Override
    public void visit(CompilationUnit compilationUnit, Object arg) {
        this.pkg = compilationUnit.getPackage().getName().toString();
        this.imports = new HashMap<>();
        for (ImportDeclaration importDeclaration: compilationUnit.getImports()) {
            if (!importDeclaration.isStatic()) {
                if (importDeclaration.isAsterisk()) {
                    String pkg = importDeclaration.getName().toString();
                    System.out.println(pkg);
                    Reflections reflections = new Reflections(pkg);
                    Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
                    System.out.println(allClasses);
                    allClasses.forEach(clazz -> this.imports.put(clazz.getName(), pkg));
                } else {
                    String name = importDeclaration.getName().toString();
                    String[] tmp = name.replace("/", ".").split("\\.");
                    String pkg = String.join(".", (CharSequence[]) Arrays.copyOfRange(tmp, 0, tmp.length - 1));
                    this.imports.put(tmp[tmp.length - 1], pkg);
                }
            }
        }
        this.imports.forEach((pkg, name) -> System.out.println(String.format("import %s.%s", pkg, name)));
        super.visit(compilationUnit, arg);
    }

    @Override
    public void visit(MethodDeclaration declaration, Object arg) {
        System.out.println();
        JavadocComment doc = declaration.getJavaDoc();
        if (doc != null) System.out.println(doc.getContent());
        Pair<String, List<String>> pair = getClassName(declaration);
        String ownerFullName = "L" + (pair.getKey().length() > 0 ? pair.getKey() + "." : "") + String.join("$", pair.getValue()) + ";";
        AstClass owner = Parser.parseClass(ownerFullName);
        String name = declaration.getName();
        List<AstType> parameters = declaration.getParameters().stream()
                .map(Parameter::getType)
                .map(this::getTypeString)
                .map(Parser::parseType)
                .collect(Collectors.toList());
        AstType type = Parser.parseType(getTypeString(declaration.getType()));
        String body = declaration.getBody().toString();
        AstMethod method = new AstMethod(name, owner, type, parameters);
        System.out.print(method);
        System.out.println(body);
        super.visit(declaration, arg);
    }

    private Pair<String, List<String>> getClassName(Node node) {
        Node parent = node.getParentNode();
        if (parent == null) {
            return new Pair<>(this.pkg, new ArrayList<>());
        }
        Pair<String, List<String>> pair = getClassName(parent);
        if (parent instanceof ClassOrInterfaceDeclaration) {
            pair.getValue().add(((ClassOrInterfaceDeclaration) parent).getName());
        }
        return pair;
    }

    private String getTypePackage(String name) {
        if (name.replace("/", ".").split("\\.").length > 0) return "";
        return "";
    }

    private String getTypeString(Type type) {
        String name = type.toString();
        if (AstPrimitiveType.isPrimitive(name)) {
            return name;
        }
        String pkg = this.getTypePackage(name);
        return "L" + pkg + name + ";";
    }
}
