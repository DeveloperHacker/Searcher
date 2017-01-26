package analysers;

import analysers.analysable.MethodDescription;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

class AsmMethodAnalyser extends MethodVisitor {

    public Consumer<MethodDescription> visitMethodInst = null;

    public AsmMethodAnalyser(int i) {
        super(i);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        MethodDescription method = Parser.parseMethod('L' + owner + ';', name, desc);
        if (this.visitMethodInst != null) this.visitMethodInst.accept(method);
    }
}
