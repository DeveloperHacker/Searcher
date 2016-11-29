package analysers;

import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;
import parts.AstMethod;

import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM5;

public class Searcher {

    final private Map<AstMethod, List<AstMethod>> methodIndex = new HashMap<>();

    public Searcher(List<String> sourceCodes, List<String> byteCodes) throws IOException {
        for (String code : byteCodes) {
            try {
                final ClassAnalyser analyser = new ClassAnalyser(ASM5);
                final ClassReader reader  = new ClassReader(code);
                reader.accept(analyser, 0);
                this.methodIndex.putAll(analyser.getMethods());
            } catch (IOException e) {
                throw new IOException(String.format("parts.AstClass '%s' not found", code));
            }
        }
    }

    public Set<AstMethod> usages(AstMethod attribute) {
        Set<AstMethod> result = new HashSet<>();
        for (Map.Entry<AstMethod, List<AstMethod>> entry : this.methodIndex.entrySet()) {
            for (AstMethod method : entry.getValue()) {
                if (method.equals(attribute)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    public Pair<AstMethod, JavadocComment> associate(AstMethod method) {
        return null;
    }

    public Map<AstMethod, List<AstMethod>> getMethodIndex() {
        return this.methodIndex;
    }
}
