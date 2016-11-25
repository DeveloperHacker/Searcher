package analysers;

import org.objectweb.asm.MethodVisitor;
import parts.Class;
import parts.Method;

import java.util.function.Consumer;

public class MethodAnalyser extends MethodVisitor {

    public Consumer<Method> visitMethodInsn = null;

    public MethodAnalyser(int i) {
        super(i);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Method method = Parser.parseMethod('L' + owner + ';', name, desc);
        if (this.visitMethodInsn != null) this.visitMethodInsn.accept(method);
    }
}
