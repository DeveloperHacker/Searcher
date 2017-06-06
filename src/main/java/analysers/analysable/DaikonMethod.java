package analysers.analysable;


import utils.Shell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DaikonMethod extends Shell<MethodDescription> {


    final private MethodDescription description;
    final public Set<String> enter = new HashSet<>();
    final public Set<String> exit = new HashSet<>();
    final public Map<Integer, Set<String>> exits = new HashMap<>();

    public DaikonMethod(MethodDescription description) {
        super(description);
        this.description = description;
    }

    public MethodDescription getDescription() {
        return description;
    }
}
