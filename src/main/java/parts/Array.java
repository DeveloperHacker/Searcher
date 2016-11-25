package parts;

public class Array extends Type {

    private Type inner;

    public Array(Type inner) {
        super("[");
        this.inner = inner;
    }

    public Type getInner() {
        return this.inner;
    }

    /**
     * @see Object
     */
    @Override
    public String toString() {
        return this.getName() + this.inner.toString();
    }
}
