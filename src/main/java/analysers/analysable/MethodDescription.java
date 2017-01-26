package analysers.analysable;

import org.javatuples.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class MethodDescription {

    private final String name;
    private final AsmClass owner;
    private final AsmType type;
    private final List<Pair<AsmType, String>> parameters;
    private final String description;

    public MethodDescription(String name, AsmClass owner, AsmType type, List<Pair<AsmType, String>> parameters) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.parameters = parameters;
        final String params = parameters.stream()
                .map(pair -> pair.getValue0().toString() + " " + pair.getValue1())
                .collect(Collectors.joining(", "));
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

    public List<Pair<AsmType, String>> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return this.description;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        if (parameters != null) {
            int hashParameters = 0;
            for (Pair<AsmType, String> pair : this.parameters) {
                final AsmType type = pair.getValue0();
                hashParameters = 31 * hashParameters + (type != null ? type.hashCode() : 0);
            }
            result = 31 * result + hashParameters;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDescription)) return false;
        final MethodDescription method = (MethodDescription) o;
        boolean paramsEq = this.parameters.size() == method.parameters.size();
        if (paramsEq) {
            for (int i = 0; i < this.parameters.size(); ++i) {
                paramsEq &= this.parameters.get(i).getValue0().equals(method.parameters.get(i).getValue0());
            }
        }
        return this.name.equals(method.name) && this.owner.equals(method.owner) && paramsEq;
    }
}
