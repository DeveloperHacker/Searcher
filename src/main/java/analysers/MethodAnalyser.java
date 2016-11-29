package analysers;

import org.objectweb.asm.MethodVisitor;
import parts.AstMethod;

import java.util.function.Consumer;

public class MethodAnalyser extends MethodVisitor {

    public Consumer<AstMethod> visitMethodInsn = null;

    public MethodAnalyser(int i) {
        super(i);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        AstMethod method = Parser.parseMethod('L' + owner + ';', name, desc);
        if (this.visitMethodInsn != null) this.visitMethodInsn.accept(method);
    }
}
