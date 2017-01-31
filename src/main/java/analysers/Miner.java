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
import packers.Unpacker;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Miner {

    private final Map<MethodDescription, Set<MethodDescription>> indexedMethods = new HashMap<>();
    private final Map<MethodDescription, Pair<AstMethod, DaikonMethod>> methods = new HashMap<>();

    private Miner() {

    }

    public Pair<AstMethod, DaikonMethod> getMethod(MethodDescription description) {
        return this.methods.get(description);
    }

    public Set<Pair<AstMethod, DaikonMethod>> getMethods() {
        return new HashSet<>(this.methods.values());
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

    public static Set<String> loadJava(String passToFolder) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes("**/*.java");
        scanner.setBasedir(passToFolder);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] codes = scanner.getIncludedFiles();
        return Arrays.stream(codes).map(code -> passToFolder + "/" + code).collect(Collectors.toSet());
    }

    private static Map<MethodDescription, Set<MethodDescription>> indexByteCodes(Set<String> byteCodes, AsmClassAnalyser analyser) throws IOException {
        for (String code : byteCodes) {
            final ClassReader reader  = new ClassReader(code);
            reader.accept(analyser, 0);
        }
        return analyser.getMethods();
    }

    private static Set<AstMethod> indexJavaCodes(Set<String> javaCodes, AbstractVisitor visitor) throws FileNotFoundException, ParseException {
        for (String code : javaCodes) {
            final FileInputStream in = new FileInputStream(code);
            final CompilationUnit ast = JavaParser.parse(in);
            visitor.visit(ast, null);
        }
        return visitor.getMethods();
    }

    public static Miner simple(Set<String> javaCodes) throws FileNotFoundException, ParseException {
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
            if (byteMethods.containsKey(method.getDescription())) usages.addAll(byteMethods.get(method.getDescription()));
            self.indexedMethods.put(description, usages);
            self.methods.put(description, new Pair<>(method, null));
        }
        return self;
    }

    public void update(Set<DaikonMethod> methods) {
        for (DaikonMethod daikonMethod : methods) {
            final MethodDescription description = daikonMethod.getDescription();
            final AstMethod astMethod = this.methods.containsKey(description) ? this.methods.get(description).getValue0() : null;
            this.methods.put(description, new Pair<>(astMethod, daikonMethod));
        }
    }

    private static void execute(
            final List<String> commands,
            final String path,
            final String input,
            final String output
    ) throws IOException, InterruptedException {
        final File script = File.createTempFile("script", null);
        final Writer streamWriter = new OutputStreamWriter(new FileOutputStream(script));
        final PrintWriter writer = new PrintWriter(streamWriter);
        for (String command : commands) writer.println(command);
        writer.close();
        final ProcessBuilder process = new ProcessBuilder("bash", script.toString());
        process.inheritIO();
        if (input != null) process.redirectInput(new File(input));
        if (output != null) process.redirectOutput(new File(output));
        if (path != null) process.directory(new File(path));
        process.start().waitFor();
    }

    public enum Type {
        PROJECT, MAVEN, JAR
    }

    public static List<Pair<AstMethod, DaikonMethod>> mine(
            final String daikon,
            final Map<String, Pair<Type, Set<String>>> projects
    ) throws IOException, InterruptedException, com.github.javaparser.ParseException {
        final List<Pair<AstMethod, DaikonMethod>> methods = new LinkedList<>();
        for (Map.Entry<String, Pair<Type, Set<String>>> project : projects.entrySet()) {
            System.out.println(String.format("[%s] %s", project.getValue().getValue0().toString(), project.getKey()));
            final String jar;
            final String path;
            final Type type = project.getValue().getValue0();
            final List<String> makeCommands = new ArrayList<>();
            makeCommands.add("#!/usr/bin/env bash");
            switch (type) {
                case PROJECT:
                    jar = null;
                    path = project.getKey();
                    makeCommands.add("shopt -s globstar");
                    makeCommands.add("javac -cp .:$LIBS **/*.java");
                    execute(makeCommands, path, null, null);
                    break;
                case MAVEN:
                    path = project.getKey();
                    makeCommands.add("mvn package");
                    execute(makeCommands, path, null, null);
                    final DirectoryScanner scanner = new DirectoryScanner();
                    scanner.setIncludes("*.jar");
                    scanner.setBasedir(path + "/target");
                    scanner.setCaseSensitive(false);
                    scanner.scan();
                    final String[] jars = scanner.getIncludedFiles();
                    jar = Arrays.stream(jars)
                            .map(name -> path + "/target/" + name)
                            .reduce((acc, name) -> acc + ":" + name)
                            .orElse("");
                    break;
                case JAR:
                    jar = project.getKey();
                    final String[] pathArr = project.getKey().split("/");
                    path = String.join("/", Arrays.copyOfRange(pathArr, 0, pathArr.length - 1));
                    break;
                default:
                    path = null;
                    jar = null;
            }
            final Set<String> mainClasses = project.getValue().getValue1();
            final Set<String> javaCodes = Miner.loadJava(path);
            final Miner miner = Miner.simple(javaCodes);
            for (String mainClass : mainClasses) {
                final String className = Parser.parseClass(String.format("L%s;", mainClass)).getName();
                final String dtrace = path + "/" + className + ".dtrace";
                final List<String> dyncompCommands = new ArrayList<>();
                final List<String> chicoryCommand = new ArrayList<>();
                dyncompCommands.add("#!/usr/bin/env bash");
                dyncompCommands.add("export DAIKONDIR=" + daikon);
                chicoryCommand.add("#!/usr/bin/env bash");
                chicoryCommand.add("export DAIKONDIR=" + daikon);
                switch (type) {
                    case PROJECT:
                        dyncompCommands.add("java -cp .:" + daikon + "/daikon.jar daikon.DynComp --no-cset-file " + mainClass);
                        chicoryCommand.add("java -cp .:" + daikon + "/daikon.jar daikon.Chicory --daikon --comparability-file=" + className + ".decls-DynComp " + mainClass);
                        break;
                    case MAVEN:
                    case JAR:
                        dyncompCommands.add("java -cp " + jar + ":" + daikon + "/daikon.jar daikon.DynComp --no-cset-file " + mainClass);
                        chicoryCommand.add("java -cp " + jar + ":" + daikon + "/daikon.jar daikon.Chicory --daikon --comparability-file=" + className + ".decls-DynComp " + mainClass);
                        break;
                }
                System.out.println(dyncompCommands);
                execute(dyncompCommands, path, null, null);
                execute(chicoryCommand, path, null, dtrace);
                miner.update(Unpacker.unpackSimple(dtrace));
                methods.addAll(miner.getMethods());
            }
            System.out.println();
        }
        return methods;
    }
}
