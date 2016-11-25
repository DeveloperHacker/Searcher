package parts;

import java.util.List;
import java.util.stream.Collectors;

public class Method {

    private String name;
    private Class owner;
    private Type type;
    private List<Type> parameters;

    public Method(String name, Class owner, Type type, List<Type> parameters) {
        this.name = name;
        this.owner = owner;
        this.type = type;
        this.parameters = parameters;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Class getOwner() {
        return owner;
    }

    public List<Type> getParameters() {
        return parameters;
    }

    public String getDescription() {
        String parameters = this.parameters.stream().map(Type::toString).collect(Collectors.joining(""));
        return String.format("%s%s(%s)%s", owner.toString(), name, parameters, type.toString());
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
        if (!(o instanceof Method)) return false;
        Method method = (Method) o;
        if (name != null ? !name.equals(method.name) : method.name != null) return false;
        if (owner != null ? !owner.equals(method.owner) : method.owner != null) return false;
        if (type != null ? !type.equals(method.type) : method.type != null) return false;
        return parameters != null ? parameters.equals(method.parameters) : method.parameters == null;

    }
}
