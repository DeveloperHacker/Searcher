package analysers.analysable;

public class AsmArray extends AsmType {

    private final AsmType inner;

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

    public boolean isArray(String name) {
        return name.length() >= 3 && name.substring(name.length() - 2, name.length()).equals("[]");
    }
}
