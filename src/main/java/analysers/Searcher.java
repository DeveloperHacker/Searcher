package analysers;

import analysers.bytecode.AsmClassAnalyser;
import analysers.java.AbstractVisitor;
import analysers.java.AstVisitor;
import analysers.java.SimpleAstVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import javassist.NotFoundException;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;
import utils.Sets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Searcher {

    private final Map<AstMethod, Set<MethodDescription>> indexedMethods = new HashMap<>();

    private Searcher() {

    }

    public Set<AstMethod> getMethods() {
        return this.indexedMethods.keySet();
    }

    private Set<AstMethod> usages(MethodDescription attribute) {
        final Set<AstMethod> result = new HashSet<>();
        for (Map.Entry<AstMethod, Set<MethodDescription>> entry : this.indexedMethods.entrySet()) {
            for (MethodDescription method : entry.getValue()) {
                if (method.equals(attribute)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    public Pair<AstMethod, Set<AstMethod>> associate(MethodDescription method) throws NotFoundException {
        AstMethod astMethod = Sets.getElement(this.indexedMethods.keySet(), method);
        return new Pair<>(astMethod, this.usages(astMethod.getDescription()));
    }

    private static Set<String> loadJava(String passToFolder) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes("**/*.java");
        scanner.setBasedir(passToFolder);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] codes = scanner.getIncludedFiles();
        return Arrays.stream(codes).map(code -> passToFolder + code).collect(Collectors.toSet());
    }

    public static Searcher simple(String passToFolder) throws FileNotFoundException {
        return simple(loadJava(passToFolder));
    }

    private static Map<MethodDescription, Set<MethodDescription>> indexByteCodes(Set<String> byteCodes, AsmClassAnalyser analyser) throws IOException {
        for (String code : byteCodes) {
            try {
                final ClassReader reader  = new ClassReader(code);
                reader.accept(analyser, 0);
            } catch (Parser.ParseException ex) {
                ex.printStackTrace();
            }
        }
        return analyser.getMethods();
    }

    private static Set<AstMethod> indexJavaCodes(Set<String> javaCodes, AbstractVisitor visitor) throws FileNotFoundException {
        for (String code : javaCodes) {
            try {
                System.out.println(code);
                final FileInputStream in = new FileInputStream(code);
                final CompilationUnit ast = JavaParser.parse(in);
                visitor.visit(ast, null);
            } catch (ParseException | Parser.ParseException ex) {
                ex.printStackTrace();
                throw new RuntimeException();
            }
        }
        return visitor.getMethods();
    }

    private static Searcher simple(Set<String> javaCodes) throws FileNotFoundException {
        final Searcher self = new Searcher();
        final Set<AstMethod> astMethods = Searcher.indexJavaCodes(javaCodes, new SimpleAstVisitor());
        for (AstMethod method : astMethods) {
            self.indexedMethods.put(method, new HashSet<>());
        }
        return self;
    }

    public static Searcher normal(Set<String> javaCodes, Set<String> byteCodes) throws IOException {
        final Searcher self = new Searcher();
        final Map<MethodDescription, Set<MethodDescription>> byteMethods = Searcher.indexByteCodes(byteCodes, new AsmClassAnalyser());
        final Set<AstMethod> astMethods = Searcher.indexJavaCodes(javaCodes, new AstVisitor());
        for (AstMethod method : astMethods) {
            if (byteMethods.containsKey(method.getDescription())) {
                self.indexedMethods.put(method, byteMethods.get(method.getDescription()));
            } else {
                self.indexedMethods.put(method, new HashSet<>());
            }
        }
        return self;
    }
}
