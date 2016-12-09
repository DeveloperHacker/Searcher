package analysers.bytecode;

public class AsmArray extends AsmType {

    private AsmType inner;

    public AsmArray(AsmType inner) {
        super("[");
        this.inner = inner;
    }

    public AsmType getInner() {
        return this.inner;
    }

    @Override
    public String toString() {
        return "[" + this.inner.toString();
    }
}
