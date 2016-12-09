package analysers;

import analysers.bytecode.AsmClassAnalyser;
import analysers.java.AstVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM5;

public class Searcher {

    final private Map<AstMethod, List<MethodDescription>> indexedMethods = new HashMap<>();

    public Searcher(List<String> javaCodes, List<String> byteCodes) throws IOException, ParseException {
        final AsmClassAnalyser analyser = new AsmClassAnalyser(ASM5);
        for (String code : byteCodes) {
            try {
                final ClassReader reader  = new ClassReader(code);
                reader.accept(analyser, 0);
            } catch (IOException e) {
                throw new IOException(String.format("Class '%s' not found", code));
            }
        }
        final AstVisitor visitor = new AstVisitor();
        for (String code: javaCodes) {
            final FileInputStream in = new FileInputStream(code);
            final CompilationUnit ast = JavaParser.parse(in);
            visitor.visit(ast, null);
        }
        final Map<MethodDescription, List<MethodDescription>> byteMethods = analyser.getMethods();
        final Set<AstMethod> astMethods = visitor.getMethods();
        for (AstMethod method : astMethods) {
            if (byteMethods.containsKey(method.getDescription())) {
                this.indexedMethods.put(method, byteMethods.get(method.getDescription()));
            } else {
                System.out.println(method.getDescription());
                this.indexedMethods.put(method, new ArrayList<>());
            }
        }
        System.out.println();
        byteMethods.keySet().forEach(System.out::println);
//        astMethods.stream().map(AstMethod::getDescription).forEach(System.out::println);
    }

    public Set<AstMethod> usages(MethodDescription attribute) {
        final Set<AstMethod> result = new HashSet<>();
        for (Map.Entry<AstMethod, List<MethodDescription>> entry : this.indexedMethods.entrySet()) {
            for (MethodDescription method : entry.getValue()) {
                if (method.equals(attribute)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    public Pair<MethodDescription, JavadocComment> associate(MethodDescription method) {

        return null;
    }
}
