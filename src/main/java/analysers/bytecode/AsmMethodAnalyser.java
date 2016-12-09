package analysers.bytecode;

import analysers.MethodDescription;
import analysers.Parser;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

public class AsmMethodAnalyser extends MethodVisitor {

    public Consumer<MethodDescription> visitMethodInsn = null;

    public AsmMethodAnalyser(int i) {
        super(i);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        MethodDescription method = Parser.parseMethod('L' + owner + ';', name, desc);
        if (this.visitMethodInsn != null) this.visitMethodInsn.accept(method);
    }
}
