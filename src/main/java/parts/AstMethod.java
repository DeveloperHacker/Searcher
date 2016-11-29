package parts;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AstMethod {

    private final String name;
    private final AstClass owner;
    private final AstType type;
    private final List<AstType> parameters;
    private Supplier<String> description;

    public AstMethod(String name, AstClass owner, AstType type, List<AstType> parameters) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.parameters = parameters;
        this.description = () -> {
            String params = parameters.stream().map(AstType::toString).collect(Collectors.joining(""));
            String val = String.format("%s%s(%s)%s", owner.toString(), name, params, type.toString());
            this.description = () -> val;
            return val;
        };
    }

    public AstType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public AstClass getOwner() {
        return owner;
    }

    public List<AstType> getParameters() {
        return parameters;
    }

    public String getDescription() {
        return this.description.get();
    }

    @Override
    public String toString() {
        return this.getDescription();
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AstMethod)) return false;
        AstMethod method = (AstMethod) o;
        if (name != null ? !name.equals(method.name) : method.name != null) return false;
        if (owner != null ? !owner.equals(method.owner) : method.owner != null) return false;
        if (type != null ? !type.equals(method.type) : method.type != null) return false;
        return parameters != null ? parameters.equals(method.parameters) : method.parameters == null;

    }
}
