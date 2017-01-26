import analysers.Parser;
import analysers.analysable.AsmClass;
import analysers.analysable.AsmType;
import analysers.analysable.DaikonMethod;
import analysers.analysable.MethodDescription;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Unpacker {

    private final static String delimiter = "===========================================================================";
    private final static String end = "Exiting Daikon.";
    private final static String typeDelimiter = ":::";
    private Unpacker() {

    }

    public static Set<DaikonMethod> unpackNormal(String pathToDtrace) throws IOException {
        return Unpacker.unpack(pathToDtrace, false);

    }

    public static Set<DaikonMethod> unpackSimple(String pathToDtrace) throws IOException {
        return Unpacker.unpack(pathToDtrace, true);
    }


    private static Set<DaikonMethod> unpack(String pathToDtrace, boolean simple) throws IOException {
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
                    MethodDescription description = Parser.parseDaikonMethodDescription(pair[0], simple);
                    if (simple) {
                        final String name = description.getName();
                        final AsmClass owner = new AsmClass(null, null, description.getOwner().getName());
                        final AsmType type = description.getType();
                        final List<Pair<AsmType, String>> parameters = description.getParameters();
                        description = new MethodDescription(name, owner, type, parameters);
                    }
                    method = new DaikonMethod(description);
                    if (!methods.containsKey(description)) {
                        methods.put(description, method);
                    } else {
                        method = methods.get(description);
                    }
                    state = State.READ_BODY;
                } else {
                    if (line.equals(end)) break;
                    if (line.equals(delimiter)) {
                        state = State.READ_HEAD;
                    } else {
                        if ("ENTER".equals(id)) {
                            method.enter.add(line);
                        }
                        else if ("EXIT".equals(id)) {
                            method.exit.add(line);
                        }
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
