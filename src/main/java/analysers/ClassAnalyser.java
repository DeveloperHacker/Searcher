package analysers;

import org.objectweb.asm.*;
import parts.Class;
import parts.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClassAnalyser extends ClassVisitor {

    private Class current;
    private Map<Method, List<Method>> methods;

    public ClassAnalyser(int api) {
        super(api);
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.current = Parser.parseClass('L' + name + ';');
        this.methods = new HashMap<>();
    }

    public void visitSource(String source, String debug) {

    }

    public void visitOuterClass(String owner, String name, String desc) {

    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {

    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {

    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        Method method = Parser.parseMethod(this.current, name, desc);
        List<Method> innerMethods = new ArrayList<>();
        MethodAnalyser methodAnalyser = new MethodAnalyser(api);
        methodAnalyser.visitMethodInsn = innerMethods::add;
        this.methods.put(method, innerMethods);
        return methodAnalyser;
    }

    public void visitEnd() {

    }

    public Map<Method, List<Method>> getMethods() {
        return this.methods;
    }
}
