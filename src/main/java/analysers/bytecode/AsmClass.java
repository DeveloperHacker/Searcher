package analysers.bytecode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AsmClass extends AsmType {

    private final String pkg;
    private final AsmClass owner;
    private Supplier<String> name;

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
    public AsmClass(List<String> path, List<String> names) {
        this(
            String.join(".", path),
            names.size() == 1 ? null : new AsmClass(path, new ArrayList<>(names.subList(0, names.size() - 1))),
            names.get(names.size() - 1)
        );
    }

    public AsmClass(String pkg, AsmClass owner, String name) {
        super(name);
        this.pkg = pkg;
        this.owner = owner;
        this.name = () -> {
            String val;
            if (owner == null) {
                val = "L" + (pkg.length() > 0 ? pkg + "." : "") + name + ";";
            } else {
                String ownerName = owner.getFullName();
                val = ownerName.substring(0, ownerName.length() - 1) + "$" + name + ";";
            }
            this.name = () -> val;
            return val;
        };
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

    /**
     * @return the full name of this class, not null
     */
    private String getFullName() {
        return this.name.get();
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
        if (!(o instanceof AsmClass)) return false;
        AsmClass aAsmClass = (AsmClass) o;
        if (pkg != null ? !pkg.equals(aAsmClass.pkg) : aAsmClass.pkg != null) return false;
        return owner != null ? owner.equals(aAsmClass.owner) : aAsmClass.owner == null;
    }
}
