package analysers;

import org.objectweb.asm.*;
import parts.AstClass;
import parts.AstMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClassAnalyser extends ClassVisitor {

    private AstClass current;
    private Map<AstMethod, List<AstMethod>> methods;

    public ClassAnalyser(int api) {
        super(api);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.current = Parser.parseClass('L' + name + ';');
        this.methods = new HashMap<>();
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        AstMethod method = Parser.parseMethod(this.current, name, desc);
        List<AstMethod> innerMethods = new ArrayList<>();
        MethodAnalyser methodAnalyser = new MethodAnalyser(api);
        methodAnalyser.visitMethodInsn = innerMethods::add;
        this.methods.put(method, innerMethods);
        return methodAnalyser;
    }

    public Map<AstMethod, List<AstMethod>> getMethods() {
        return this.methods;
    }
}
