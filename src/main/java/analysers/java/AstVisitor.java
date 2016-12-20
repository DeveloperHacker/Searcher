package analysers.java;

import analysers.AstMethod;
import analysers.MethodDescription;
import analysers.MethodParts;
import analysers.Parser;
import analysers.bytecode.AsmClass;
import analysers.bytecode.AsmPrimitiveType;
import analysers.bytecode.AsmType;
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
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class AstVisitor extends VoidVisitorAdapter<Object> {

    private String pkg;
    private Map<String, String> imports;
    private Set<AstMethod> methods = new HashSet<>();

    @Override
    public void visit(CompilationUnit compilationUnit, Object arg) {
        this.pkg = compilationUnit.getPackage() == null ? "" : compilationUnit.getPackage().getName().toString();
        this.imports = new HashMap<>();
        for (ImportDeclaration importDeclaration: compilationUnit.getImports()) {
            if (!importDeclaration.isStatic()) {
                if (importDeclaration.isAsterisk()) {
                    final String pkg = importDeclaration.getName().toString();
                    addImport(pkg, "*");
                } else {
                    final String clazz = importDeclaration.getName().toString();
                    final String[] clazzArr = clazz.replace("/", ".").split("\\.");
                    final String pkg = String.join(".", (CharSequence[]) Arrays.copyOfRange(clazzArr, 0, clazzArr.length - 1));
                    addImport(pkg, clazzArr[clazzArr.length - 1]);
                }
            }
        }
        addImport("java.lang", "*");
        addImport(pkg, "*");
        if (!this.imports.containsKey("Object")) {
            addImport("java.lang", "Object");
        }
        super.visit(compilationUnit, arg);
    }

    private boolean addImport(String pkg, String clazz) {
        if (clazz.equals("*")) {
            final String[] pkgArr = pkg.split("\\.");
            try {
                if (!this.imports.containsValue(pkg)) {
                    final List<ClassLoader> classLoadersList = new LinkedList<>();
                    classLoadersList.add(ClasspathHelper.contextClassLoader());
                    classLoadersList.add(ClasspathHelper.staticClassLoader());
                    final Reflections reflections = new Reflections(new ConfigurationBuilder()
                            .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                            .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                            .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg))));
                    for (String className : reflections.getAllTypes()) {
                        final String[] clazzArr = className.split("[\\.\\$]");
                        if (clazzArr.length == pkgArr.length + 1) {
                            this.imports.put(clazzArr[pkgArr.length], pkg);
                        }
                    }
                }
            } catch (ReflectionsException ex) {
                System.err.println(String.format("Package %s not linked", pkg));
                return false;
            }
        } else {
            this.imports.put(clazz, pkg);
        }
        return true;
    }

    @Override
    public void visit(MethodDeclaration declaration, Object arg) {
        final JavadocComment doc = declaration.getJavaDoc();
        final Pair<String, List<String>> pair = getClassName(declaration);
        final String ownerFullName = "L" + (pair.getKey().length() > 0 ? pair.getKey() + "." : "") + String.join("$", pair.getValue()) + ";";
        final AsmClass owner = Parser.parseClass(ownerFullName);
        final String name = declaration.getName();
        final List<AsmType> parameters = declaration.getParameters().stream()
                .map(Parameter::getType)
                .map(this::getTypeString)
                .map(Parser::parseType)
                .collect(Collectors.toList());
        final AsmType type = Parser.parseType(getTypeString(declaration.getType()));
        final String body = declaration.getBody().toString();
        final MethodDescription method = new MethodDescription(name, owner, type, parameters);
        this.methods.add(new AstMethod(method, new MethodParts(doc, body)));
        super.visit(declaration, arg);
    }

    private Pair<String, List<String>> getClassName(Node node) {
        final Node parent = node.getParentNode();
        if (parent == null) {
            return new Pair<>(this.pkg, new ArrayList<>());
        }
        final Pair<String, List<String>> pair = getClassName(parent);
        if (parent instanceof ClassOrInterfaceDeclaration) {
            pair.getValue().add(((ClassOrInterfaceDeclaration) parent).getName());
        }
        return pair;
    }

    private String getTypePackage(String name) {
        name = name.split("<")[0];
        return this.imports.containsKey(name) ? this.imports.get(name) : "";
    }

    private String getTypeString(Type type) {
        final String fullName = type.toString().replace('.', '$');
        final String[] arrName = fullName.split("\\$");
        final String name = arrName[0];
        final String pkg = this.getTypePackage(name);
        if (pkg.length() == 0 && AsmPrimitiveType.isPrimitive(name)) {
            return AsmPrimitiveType.shortRepresentation(name).toString();
        }
        return String.format("L%s.%s;", pkg, fullName);
    }

    public Set<AstMethod> getMethods() {
        return this.methods;
    }

    public void setMethods(Set<AstMethod> methods) {
        this.methods = methods;
    }
}
