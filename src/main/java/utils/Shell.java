package utils;

public abstract class Shell<E> {

    private final E inner;

    public Shell(final E inner) {
        this.inner = inner;
    }

    E inner() {
        return this.inner;
    }

    @Override
    public int hashCode() {
        return this.inner().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Shell) {
            final Shell shell = (Shell) o;
            return this.inner().equals(shell.inner());
        }
        if (o == null) return false;
        try {
            @SuppressWarnings("unchecked")
            final E inner = (E) o;
            return this.inner().equals(inner);
        } catch (ClassCastException ignored) {
        }
        return false;
    }
}