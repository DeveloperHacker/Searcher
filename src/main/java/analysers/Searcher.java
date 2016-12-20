package analysers;

import analysers.bytecode.AsmClassAnalyser;
import analysers.java.AstVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import javassist.NotFoundException;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ASM5;

public class Searcher {

    private final Map<AstMethod, Set<MethodDescription>> indexedMethods = new HashMap<>();

    private Searcher() {

    }

    public Set<AstMethod> usages(MethodDescription attribute) {
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
        AstMethod astMethod = null;
        for (AstMethod m : this.indexedMethods.keySet()) {
            if (m.getDescription().equals(method)) {
                astMethod = m;
                break;
            }
        }
        if (astMethod == null) {
            throw new NotFoundException(String.format("Method with decryption %s not found", method.toString()));
        }
        return new Pair<>(astMethod, this.usages(astMethod.getDescription()));
    }

    public static Set<String> searchCodes(String passToFolder, String extension) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes("**\\*." + extension);
        scanner.setBasedir(passToFolder);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] codes = scanner.getIncludedFiles();
        return Arrays.stream(codes).collect(Collectors.toSet());
    }

    public static Searcher simple(String passToFolder) throws Parser.ParseException {
        return simple(searchCodes(passToFolder, "java"));
    }

    public static Searcher simple(Set<String> javaCodes) {
        final Searcher self = new Searcher();
        return self;
    }

    public static Searcher normal(Set<String> javaCodes, Set<String> byteCodes) throws IOException {
        final Searcher self = new Searcher();
        final AsmClassAnalyser analyser = new AsmClassAnalyser(ASM5);
        for (String code : byteCodes) {
            try {
                final ClassReader reader  = new ClassReader(code);
                reader.accept(analyser, 0);
            } catch (Parser.ParseException ex) {
                ex.printStackTrace();
            }
        }
        final AstVisitor visitor = new AstVisitor();
        for (String code: javaCodes) {
            try {
                final FileInputStream in = new FileInputStream(code);
                final CompilationUnit ast = JavaParser.parse(in);
                visitor.visit(ast, null);
            } catch (ParseException | Parser.ParseException ex) {
                ex.printStackTrace();
            }
        }
        final Map<MethodDescription, Set<MethodDescription>> byteMethods = analyser.getMethods();
        final Set<AstMethod> astMethods = visitor.getMethods();
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
