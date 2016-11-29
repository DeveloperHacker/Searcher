package parts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AstClass extends AstType {

    private final String pkg;
    private final AstClass owner;
    private Supplier<String> fullname;

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
    public AstClass(List<String> path, List<String> names) {
        this(
            String.join(".", path),
            names.size() == 1 ? null : new AstClass(path, new ArrayList<>(names.subList(0, names.size() - 1))),
            names.get(names.size() - 1)
        );
    }

    public AstClass(String pkg, AstClass owner, String name) {
        super(name);
        this.pkg = pkg;
        this.owner = owner;
        this.fullname = () -> {
            String val;
            if (owner == null) {
                val = "L" + (pkg.length() > 0 ? pkg + "." : "")+ name + ";";
            } else {
                String ownerName = owner.getFullName();
                val = ownerName.substring(0, ownerName.length() - 1) + "$" + name + ";";
            }
            this.fullname = () -> val;
            return val;
        };
    }

    /**
     * @return owner of this class, may be null
     */
    public AstClass getOwner() {
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
        return this.fullname.get();
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
        if (!(o instanceof AstClass)) return false;
        AstClass aAstClass = (AstClass) o;
        if (pkg != null ? !pkg.equals(aAstClass.pkg) : aAstClass.pkg != null) return false;
        return owner != null ? owner.equals(aAstClass.owner) : aAstClass.owner == null;
    }
}
