package analysers;

import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import analysers.analysable.MethodDescription;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import javassist.NotFoundException;
import org.apache.maven.shared.utils.io.DirectoryScanner;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Miner {

    private final Map<MethodDescription, Set<MethodDescription>> indexedMethods = new HashMap<>();
    private final Map<MethodDescription, Pair<AstMethod, DaikonMethod>> methods = new HashMap<>();

    private Miner() {

    }

    private Pair<AstMethod, DaikonMethod> getMethod(MethodDescription description) {
        return this.methods.get(description);
    }

    public Collection<Pair<AstMethod, DaikonMethod>> getMethods() {
        return this.methods.values();
    }

    private Set<MethodDescription> usages(MethodDescription description) {
        return this.indexedMethods.get(description);
    }

    public Pair<Pair<AstMethod, DaikonMethod>, Set<Pair<AstMethod, DaikonMethod>>> associate(MethodDescription description) throws NotFoundException {
        if (!this.methods.containsKey(description)) return null;
        final Pair<AstMethod, DaikonMethod> pair = this.methods.get(description);
        final Set<Pair<AstMethod, DaikonMethod>> usages = this.usages(description).stream().map(this::getMethod).collect(Collectors.toSet());
        return new Pair<>(pair, usages);
    }

    private static Set<String> loadJava(String path) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes("**/*.java");
        scanner.setBasedir(path);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] codes = scanner.getIncludedFiles();
        return Arrays.stream(codes).map(code -> path + "/" + code).collect(Collectors.toSet());
    }

    private static Map<MethodDescription, Set<MethodDescription>> indexByteCodes(Set<String> byteCodes, AsmClassAnalyser analyser) throws IOException {
        for (String code : byteCodes) {
            final ClassReader reader = new ClassReader(code);
            reader.accept(analyser, 0);
        }
        return analyser.getMethods();
    }

    private static Set<AstMethod> indexJavaCodes(Set<String> javaCodes, AbstractVisitor visitor) throws FileNotFoundException, ParseException {
        for (String code : javaCodes) {
            System.out.println(code);
            final FileInputStream in = new FileInputStream(code);
            final CompilationUnit ast = JavaParser.parse(in);
            visitor.visit(ast, null);
        }
        return visitor.getMethods();
    }

    private static Miner simple(Set<String> javaCodes) throws FileNotFoundException, ParseException {
        final Miner self = new Miner();
        final Set<AstMethod> astMethods = Miner.indexJavaCodes(javaCodes, new SimpleAstVisitor());
        for (AstMethod method : astMethods) {
            final MethodDescription description = method.getDescription();
            self.indexedMethods.put(description, new HashSet<>());
            self.methods.put(description, new Pair<>(method, null));
        }
        return self;
    }

    public static Miner normal(Set<String> javaCodes, Set<String> byteCodes) throws IOException, ParseException {
        final Miner self = new Miner();
        final Map<MethodDescription, Set<MethodDescription>> byteMethods = Miner.indexByteCodes(byteCodes, new AsmClassAnalyser());
        final Set<AstMethod> astMethods = Miner.indexJavaCodes(javaCodes, new AstVisitor());
        for (AstMethod method : astMethods) {
            final MethodDescription description = method.getDescription();
            final Set<MethodDescription> usages = new HashSet<>();
            if (byteMethods.containsKey(method.getDescription()))
                usages.addAll(byteMethods.get(method.getDescription()));
            self.indexedMethods.put(description, usages);
            self.methods.put(description, new Pair<>(method, null));
        }
        return self;
    }

    public void update(Collection<DaikonMethod> methods) {
        for (DaikonMethod daikonMethod : methods) {
            final MethodDescription description = daikonMethod.getDescription();
            final AstMethod astMethod = this.methods.containsKey(description) ? this.methods.get(description).getValue0() : null;
            this.methods.put(description, new Pair<>(astMethod, daikonMethod));
        }
    }

    public static Collection<AstMethod> mine(String path) throws FileNotFoundException, ParseException {
        final Set<String> javaCodes = Miner.loadJava(path);
        final Miner miner = Miner.simple(javaCodes);
        return miner.methods.values().stream().map(Pair::getValue0).collect(Collectors.toList());
    }

    public static Collection<Pair<AstMethod, DaikonMethod>> associate(
            final Collection<AstMethod> astMethods,
            final Collection<DaikonMethod> daikonMethods
    ) {
        final Map<MethodDescription, Pair<AstMethod, DaikonMethod>> methods = new HashMap<>();
        for (AstMethod astMethod : astMethods) {
            methods.put(astMethod.getDescription(), new Pair<>(astMethod, null));
        }
        for (DaikonMethod daikonMethod : daikonMethods) {
            final MethodDescription description = daikonMethod.getDescription();
            final AstMethod astMethod = methods.containsKey(description) ? methods.get(description).getValue0() : null;
            methods.put(description, new Pair<>(astMethod, daikonMethod));
        }
        return methods.values();
    }
}
