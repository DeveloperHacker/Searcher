package analysers.bytecode;

public abstract class AsmType {

    private final String name;

    public AsmType(String name) {
        this.name = name;
    }

    /**
     * @return name of * this type, not null
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
        if (!(o instanceof AsmType)) return false;
        AsmType type = (AsmType) o;
        return name != null ? name.equals(type.name) : type.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
