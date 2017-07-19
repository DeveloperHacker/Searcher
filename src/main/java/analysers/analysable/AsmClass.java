package analysers.analysable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AsmClass extends AsmType {

    private final String pkg;
    private final AsmClass owner;
    private Supplier<String> fullName;

    /**
     * Full fullName of class should parse to two parts (path and names).
     * For example, exist:
     * full_name = "main/printers/analysers.Parser$A$B"
     * it should parse to:
     * path = {"main", "printers"}
     * names = {"analysers.Parser", "A", "B"}
     *
     * @param path  it is path part, not null
     * @param names it is names part, not null and the length of this param is greater than 1.
     */
    public AsmClass(List<String> path, List<String> names) {
        this(
                path.size() == 0 ? null : String.join(".", path),
                names.size() == 1 ? null : new AsmClass(path, new ArrayList<>(names.subList(0, names.size() - 1))),
                names.get(names.size() - 1)
        );
    }

    public AsmClass(String pkg, AsmClass owner, String name) {
        super(full_name(pkg, owner, name));
        this.pkg = (pkg == null || pkg.length() == 0) ? null : pkg;
        this.owner = owner;
    }

    /**
     * @return owner of this class, may be null
     */
    public AsmClass getOwner() {
        return owner;
    }

    /**
     * @return the package that contains this class, not null
     */
    public String getPkg() {
        return pkg;
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
        if (!(o instanceof AsmClass)) return false;
        if (!super.equals(o)) return false;
        AsmClass asmClass = (AsmClass) o;
        return (pkg != null ? pkg.equals(asmClass.pkg) : asmClass.pkg == null) && (owner != null ? owner.equals(asmClass.owner) : asmClass.owner == null);
    }

    private static String full_name(String pkg, AsmClass owner, String name) {
        if (owner == null) {
            return "L" + (pkg == null || pkg.length() == 0 ? "" : pkg + ".") + name + ";";
        } else {
            final String ownerName = owner.getName();
            return ownerName.substring(0, ownerName.length() - 1) + "$" + name + ";";
        }
    }
}
