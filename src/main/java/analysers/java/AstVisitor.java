package analysers.java;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import javafx.util.Pair;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;

public class AstVisitor extends AbstractVisitor {

    private String pkg;
    private Map<String, String> imports;

    @Override
    public void visit(CompilationUnit compilationUnit, Object arg) {
        this.pkg = compilationUnit.getPackage() == null ? "" : compilationUnit.getPackage().getName().toString();
        System.out.println(compilationUnit.getClass().getName());
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
        addImport(this.pkg, "*");
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
    protected String getTypePackage(String name) {
        name = name.split("<")[0];
        return this.imports.containsKey(name) ? this.imports.get(name) : null;
    }

    @Override
    protected Pair<String, List<String>> getFullClassName(Node node) {
        final Node parent = node.getParentNode();
        if (parent == null) {
            return new Pair<>(this.pkg, new ArrayList<>());
        }
        final Pair<String, List<String>> pair = getFullClassName(parent);
        if (parent instanceof ClassOrInterfaceDeclaration) {
            pair.getValue().add(((ClassOrInterfaceDeclaration) parent).getName());
        }
        return pair;
    }
}
