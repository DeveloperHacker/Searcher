package analysers;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DaikonMethod {


    final private MethodDescription description;
    final public Set<String> enter = new HashSet<>();
    final public Set<String> exit = new HashSet<>();
    final public Map<Integer, Set<String>> exits = new HashMap<>();

    public DaikonMethod(MethodDescription description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format(
                "DaikonMethod{description=%s, enter=%s, exit=%s, exits=%s}",
                description.toString(),
                enter.toString(),
                exit.toString(),
                exits.toString());
    }

    public MethodDescription getDescription() {
        return description;
    }
}
