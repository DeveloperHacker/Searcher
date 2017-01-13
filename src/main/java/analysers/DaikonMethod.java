package analysers;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DaikonMethod {

    final public MethodDescription description;
    final public Set<String> enter = new HashSet<>();
    final public Set<String> exit = new HashSet<>();
    final public Map<Integer, Set<String>> exits = new HashMap<>();

    public DaikonMethod(MethodDescription description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DaikonMethod{" +
                "description=" + description +
                ", enter=" + enter +
                ", exit=" + exit +
                ", exits=" + exits +
                '}';
    }
}
