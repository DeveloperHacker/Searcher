import analysers.Miner;
import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import org.javatuples.Pair;
import packers.Packer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


class Main {

    public static void main(final String args[]) {
        try {
            final Map<String, String> arguments = arguments(args);
            final String pwd = System.getProperty("user.dir");
            assert arguments.containsKey("--daikon") : "Expected modifier --daikon";
            final String daikon = arguments.get("--daikon");
            final String config = arguments.containsKey("--config") ? arguments.get("--config") : (pwd + "/config.cfg");
            final String xml = (arguments.containsKey("--output") ? arguments.get("--output") : pwd) + "/methods.xml";
            final Map<String, Pair<Miner.Type, Set<String>>> projects = projects(config);
            final List<Pair<AstMethod, DaikonMethod>> methods = Miner.mine(daikon, projects);
            Packer.packMethods(xml, methods);
        } catch (Throwable ex) {
            System.out.println(ex.getMessage());
        }
    }

    private final static String commentLine = "//";
    private final static String commentLeft = "/*";
    private final static String commentRight = "*/";
    private final static String assign = "=";
    private final static String bracketLeft = "{";
    private final static String bracketRight = "}";

    private static Pair<List<Pair<String, Integer>>, Integer> tokenizer(final String path) throws IOException {
        final List<Pair<String, Integer>> tokens = new ArrayList<>();
        int lines = 0;
        try (final BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                final PrimitiveIterator.OfInt it = line.replaceAll(" +", " ").trim().chars().iterator();
                char prev = ' ';
                StringBuilder token = new StringBuilder();
                while (it.hasNext()) {
                    char ch = (char) it.nextInt();
                    if (ch == ' ' && prev != '\\') {
                        tokens.add(new Pair<>(token.toString(), lines));
                        token = new StringBuilder();
                    } else {
                        token.append(ch);
                    }
                    prev = ch;
                }
                if (token.length() > 0) tokens.add(new Pair<>(token.toString(), lines));
            }
        }
        return new Pair<>(tokens, lines);
    }

    private enum State {
        INIT, TYPE, HEAD, ASSIGN, START, BODY
    }

    private static Map<String, Pair<Miner.Type, Set<String>>> projects(final String path) throws IOException {
        final Map<String, Pair<Miner.Type, Set<String>>> projects = new HashMap<>();
        final Pair<List<Pair<String, Integer>>, Integer> result = tokenizer(path);
        final List<Pair<String, Integer>> tokens = result.getValue0();
        final int lines = result.getValue1();
        boolean comment = false;
        int comment_line = -1;
        Miner.Type type = null;
        String projectPath = null;
        Set<String> mainClasses = null;
        State state = State.INIT;
        for (final Pair<String, Integer> pair: tokens) {
            final String token = pair.getValue0();
            final int line = pair.getValue1();
            comment &= !commentRight.equals(token);
            if (!comment && comment_line != line && !commentRight.equals(token)) {
                if (commentLine.equals(token)) comment_line = line;
                else if (commentLeft.equals(token)) comment = true;
                else
                switch (state) {
                    case INIT:
                        assert token.length() > 2 : String.format("[Config %s] -> Syntax error: expected type in line: %d", path, line);
                        type = Miner.Type.valueOf(token.toUpperCase().substring(1, token.length() - 1));
                        state = State.HEAD;
                        break;
                    case TYPE:
                        projects.put(projectPath, new Pair<>(type, mainClasses));
                        assert token.length() > 2 : String.format("[Config %s] -> Syntax error: expected type in line: %d", path, line);
                        type = Miner.Type.valueOf(token.toUpperCase().substring(1, token.length() - 1));
                        state = State.HEAD;
                        break;
                    case HEAD:
                        projectPath = token;
                        state = State.ASSIGN;
                        break;
                    case ASSIGN:
                        assert assign.equals(token) : String.format("[Config %s] -> Syntax error: expected symbol '%s' in line: %d", path, assign, line);
                        state = State.START;
                        break;
                    case START:
                        assert bracketLeft.equals(token) : String.format("[Config %s] -> Syntax error: expected symbol '%s' in line: %d", path, bracketLeft, line);
                        mainClasses = new HashSet<>();
                        state = State.BODY;
                        break;
                    case BODY:
                        if (bracketRight.equals(token)) state = State.TYPE;
                        else mainClasses.add(token);
                        break;
                }
            }
        }
        assert state == State.TYPE || state == State.ASSIGN : String.format("[Config %s] -> Syntax error: expected symbol '%s' before EOF in line: %d", path, bracketRight, lines);
        if (projectPath != null) projects.put(projectPath, new Pair<>(type, mainClasses));
        return projects;
    }

    private static Map<String, String> arguments(final String[] args) {
        final Map<String, String> result = new HashMap<>();
        for (String arg : args) {
            final String[] argArr = arg.split("=");
            final String name = argArr[0];
            final String value = argArr.length == 1 ? null : String.join("=", Arrays.copyOfRange(argArr, 1, argArr.length));
            result.put(name, value);
        }
        return result;
    }
}

