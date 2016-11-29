package parts;

public abstract class AstType {

    private final String name;

    public AstType(String name) {
        this.name = name;
    }

    /**
     * @return name of this type, not null
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AstType)) return false;
        AstType type = (AstType) o;
        return name != null ? name.equals(type.name) : type.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
