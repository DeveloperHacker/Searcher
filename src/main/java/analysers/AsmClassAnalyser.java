package analysers;

import analysers.analysable.AsmClass;
import analysers.analysable.MethodDescription;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.*;


public class AsmClassAnalyser extends ClassVisitor {

    private AsmClass current;
    private Map<MethodDescription, Set<MethodDescription>> methods = new HashMap<>();

    public AsmClassAnalyser() {
        super(org.objectweb.asm.Opcodes.ASM5);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.current = Parser.parseClass('L' + name + ';');
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodDescription method = Parser.parseMethod(this.current, name, desc);
        final Set<MethodDescription> innerMethods = new HashSet<>();
        final AsmMethodAnalyser asmMethodAnalyser = new AsmMethodAnalyser(api);
        asmMethodAnalyser.visitMethodInst = innerMethods::add;
        this.methods.put(method, innerMethods);
        return asmMethodAnalyser;
    }

    public Map<MethodDescription, Set<MethodDescription>> getMethods() {
        return this.methods;
    }

    public void setMethods(Map<MethodDescription, Set<MethodDescription>> methods) {
        this.methods = methods;
    }
}
