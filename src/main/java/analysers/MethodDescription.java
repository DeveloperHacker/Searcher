package analysers;

import analysers.bytecode.AsmClass;
import analysers.bytecode.AsmType;

import java.util.List;
import java.util.stream.Collectors;

public class MethodDescription {

    private final String name;
    private final AsmClass owner;
    private final AsmType type;
    private final List<AsmType> parameters;
    private final String description;

    public MethodDescription(String name, AsmClass owner, AsmType type, List<AsmType> parameters) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.parameters = parameters;
        final String params = parameters.stream().map(AsmType::toString).collect(Collectors.joining(""));
        this.description = String.format("%s%s(%s)%s", owner.toString(), name, params, type.toString());
    }

    public AsmType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public AsmClass getOwner() {
        return owner;
    }

    public List<AsmType> getParameters() {
        return parameters;
    }

    public String getDescription() {
        return this.description;
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
        if (!(o instanceof MethodDescription)) return false;
        MethodDescription method = (MethodDescription) o;
        return description.equals(method.description);
    }
}
