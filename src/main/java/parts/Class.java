package parts;

import java.util.ArrayList;
import java.util.List;

public class Class extends Type {

    private final String pkg;
    private final Class owner;

    /**
     * Full name of class should parse to two parts (path and names).
     * For example, exist:
     *  full_name = "main/printers/analysers.Parser$A$B"
     * it should parse to:
     *  path = {"main", "printers"}
     *  names = {"analysers.Parser", "A", "B"}
     *
     * @param path it is path part, not null
     * @param names it is names part, not null and the length of this param is greater than 1.
     */
    public Class(List<String> path, List<String> names) {
        this(
            String.join(".", path),
            names.size() == 1 ? null : new Class(path, new ArrayList<>(names.subList(0, names.size() - 1))),
            names.get(names.size() - 1)
        );
    }

    public Class(String pkg, Class owner, String name) {
        super(name);
        this.pkg = pkg;
        this.owner = owner;
    }

    /**
     * @return owner of this class, may be null
     */
    public Class getOwner() {
        return owner;
    }

    /**
     * @return the package that contains this class, not null
     */
    public String getPkg() {
        return pkg;
    }

    /**
     * @return the full name of this class, not null
     */
    public String getFullName() {
        if (this.owner == null) {
            return "L" + this.pkg + "." + this.getName() + ";";
        } else {
            String ownerName = this.owner.getFullName();
            return ownerName.substring(0, ownerName.length() - 1) + "$" + this.getName() + ";";
        }
    }

    @Override
    public String toString() {
        return this.getFullName();
    }

    @Override
    public int hashCode() {
        int result = pkg != null ? pkg.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Class)) return false;
        Class aClass = (Class) o;
        if (pkg != null ? !pkg.equals(aClass.pkg) : aClass.pkg != null) return false;
        return owner != null ? owner.equals(aClass.owner) : aClass.owner == null;
    }
}
