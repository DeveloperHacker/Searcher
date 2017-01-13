import analysers.DaikonMethod;
import analysers.MethodDescription;
import analysers.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Unpacker {

    private final static String delimiter = "===========================================================================";
    private final static String end = "Exiting Daikon.";
    private final static String typeDelimiter = ":::";
    private Unpacker() {

    }

    public static Set<DaikonMethod> unpack(String pathToDtrace) throws IOException {
        final Map<MethodDescription, DaikonMethod> methods = new HashMap<>();
        State state = State.INIT;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToDtrace))) {
            String line;
            String id = null;
            DaikonMethod method = null;
            while ((line = reader.readLine()) != null) {
                if (state == State.INIT) {
                    if (line.equals(delimiter)) state = State.READ_HEAD;
                } else if (state == State.READ_HEAD) {
                    final String[] pair = line.split(typeDelimiter);
                    if (pair.length != 2) throw new IllegalArgumentException();
                    id = pair[1];
                    if ("OBJECT".equals(id) || "CLASS".equals(id)) {
                        state = State.INIT;
                        continue;
                    }
                    final MethodDescription description = Parser.parseDaikonMethodDescription(pair[0]);
                    method = new DaikonMethod(description);
                    if (!methods.containsKey(description)) methods.put(description, method);
                    state = State.READ_BODY;
                } else {
                    if (line.equals(end)) break;
                    if (line.equals(delimiter)) {
                        state = State.READ_HEAD;
                    } else {
                        if ("ENTER".equals(id)) method.enter.add(line);
                        else if ("EXIT".equals(id)) method.exit.add(line);
                        else {
                            final Pattern pattern = Pattern.compile("EXIT([0-9]+)");
                            final Matcher matcher = pattern.matcher(id);
                            if (matcher.find()) {
                                final Integer number = Integer.valueOf(matcher.group(1));
                                if (!method.exits.containsKey(number)) method.exits.put(number, new HashSet<>());
                                method.exits.get(number).add(line);
                            }
                        }
                    }
                }
            }
        }
        return new HashSet<>(methods.values());
    }

    private enum State {
        INIT, READ_HEAD, READ_BODY
    }
}
