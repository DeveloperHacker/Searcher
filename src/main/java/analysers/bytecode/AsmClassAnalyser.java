package analysers.bytecode;

import analysers.MethodDescription;
import analysers.Parser;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.*;


public class AsmClassAnalyser extends ClassVisitor {

    private AsmClass current;
    private Map<MethodDescription, List<MethodDescription>> methods = new HashMap<>();

    public AsmClassAnalyser(int api) {
        super(api);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.current = Parser.parseClass('L' + name + ';');
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodDescription method = Parser.parseMethod(this.current, name, desc);
        final List<MethodDescription> innerMethods = new ArrayList<>();
        final AsmMethodAnalyser asmMethodAnalyser = new AsmMethodAnalyser(api);
        asmMethodAnalyser.visitMethodInsn = innerMethods::add;
        this.methods.put(method, innerMethods);
        return asmMethodAnalyser;
    }

    public Map<MethodDescription, List<MethodDescription>> getMethods() {
        return this.methods;
    }

    public void setMethods(Map<MethodDescription, List<MethodDescription>> methods) {
        this.methods = methods;
    }
}
