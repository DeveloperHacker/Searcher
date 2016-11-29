package parts;

public class AstArray extends AstType {

    private AstType inner;

    public AstArray(AstType inner) {
        super("[");
        this.inner = inner;
    }

    public AstType getInner() {
        return this.inner;
    }

    /**
     * @see Object
     */
    @Override
    public String toString() {
        return "[" + this.inner.toString();
    }
}
