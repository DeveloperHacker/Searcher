package analysers;

import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;
import org.objectweb.asm.ClassReader;
import parts.Method;

import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.Opcodes.ASM5;

public class Searcher {

    final private Map<Method, List<Method>> methodIndex = new HashMap<>();

    public Searcher(List<String> sourceCodes, List<String> byteCodes) throws IOException {
        for (String code : byteCodes) {
            try {
                final ClassAnalyser analyser = new ClassAnalyser(ASM5);
                final ClassReader reader  = new ClassReader(code);
                reader.accept(analyser, 0);
                this.methodIndex.putAll(analyser.getMethods());
            } catch (IOException e) {
                throw new IOException(String.format("parts.Class '%s' not found", code));
            }
        }
    }

    public Set<Method> usages(Method attribute) {
        Set<Method> result = new HashSet<>();
        for (Map.Entry<Method, List<Method>> entry : this.methodIndex.entrySet()) {
            for (Method method : entry.getValue()) {
                if (method.equals(attribute)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    public Pair<Method, JavadocComment> associate(Method method) {
        return null;
    }

    public Map<Method, List<Method>> getMethodIndex() {
        return this.methodIndex;
    }
}
