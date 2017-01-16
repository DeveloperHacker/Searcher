package analysers;

import analysers.bytecode.AsmArray;
import analysers.bytecode.AsmClass;
import analysers.bytecode.AsmPrimitiveType;
import analysers.bytecode.AsmType;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {

    @FunctionalInterface
    private interface Function<X, Y, Z> {
        Z apply(X x, Y y);
    }

    private static <R> R parseHeader(String header, Function<Character, PrimitiveIterator.OfInt, R> parse) {
        try {
            final PrimitiveIterator.OfInt it = header.chars().iterator();
            char ch = (char) it.nextInt();
            try {
                return parse.apply(ch, it);
            } catch (ParseException ex) {
                throw ex.parsable == null ? new ParseException(ex.message, header) : ex;
            }
        } catch (NoSuchElementException exception) {
            throw new ParseException("Not expected 'eof'", header);
        }
    }

    private static Pair<List<String>, String> parseDescription(String description) {
        return Parser.parseHeader(description, Parser::parseDescription);
    }

    private static Pair<List<String>, String> parseDescription(char ch, PrimitiveIterator.OfInt it) {
        final List<String> parameters = new ArrayList<>();
        final StringBuilder type = new StringBuilder();

        if (ch != '(') throw new ParseException(String.format("Expected symbol '(' but not %s", ch), null);
        StringBuilder token = new StringBuilder();
        int brackets = 0;
        while (true) {
            ch = (char) it.nextInt();
            if (ch == '>') brackets--;
            if (ch == '<') brackets++;
            if (ch == ')') break;
            token.append(ch);
            if ((brackets == 0 && ch == ';') || (token.length() == 1 && AsmPrimitiveType.isPrimitive(ch))) {
                if (token.length() == 0) throw new ParseException("Expected 'type name' or '>'", null);
                parameters.add(token.toString());
                token = new StringBuilder();
            }
        }
        if (brackets > 0) throw new ParseException("Expected '>' before ')'", null);
        brackets = 0;
        while (it.hasNext()) {
            ch = (char) it.nextInt();
            if (ch == '>') brackets--;
            if (ch == '<') brackets++;
            type.append(ch);
            if (brackets == 0 && ch == ';') break;
        }
        if (brackets > 0) throw new ParseException("Expected '>' before 'eof'", null);
        if (it.hasNext()) throw new ParseException("Expected 'eof'", null);
        return new Pair<>(parameters, type.toString());
    }

    public static Triplet<Map<String, String>, List<String>, String> parseSignature(String signature) {
        return Parser.parseHeader(signature, Parser::parseSignature);
    }

    private static Triplet<Map<String, String>, List<String>, String> parseSignature(char ch, PrimitiveIterator.OfInt it) {
        final Map<String, String> generics = new HashMap<>();
        if (ch == '<') {
            StringBuilder generic = new StringBuilder();
            int brackets = 1;
            while (true) {
                ch = (char) it.nextInt();
                if (ch == '>') brackets--;
                if (ch == '<') brackets++;
                if (brackets == 0) break;
                generic.append(ch);
                if (brackets == 1 && ch == ';') {
                    if (generic.length() == 0) throw new ParseException("Expected 'type name' or '>'", null);
                    String[] pair = generic.toString().split(":{1,2}");
                    if (pair.length != 2)
                        throw new ParseException(String.format("Expected generic declaration but not %s", generic), null);
                    generics.put(pair[0], pair[1].replace("/", "."));
                    generic = new StringBuilder();
                }
            }
            if (generic.length() != 0) throw new ParseException("Expected ';' before '>'", null);
            ch = (char) it.nextInt();
        }
        final Pair<List<String>, String> pair = Parser.parseDescription(ch, it);
        final List<String> parameters = pair.getValue0();
        final String type = pair.getValue1();
        return new Triplet<>(generics, parameters, type);
    }

    public static List<String> parseGenerics(String generic) {
        final PrimitiveIterator.OfInt it = generic.replace(" ", "").chars().iterator();
        char ch = (char) it.nextInt();
        try {
            return Parser.parseGenerics(ch, it);
        } catch (ParseException ex) {
            throw ex.parsable == null ? new ParseException(ex.message, generic) : ex;
        }
    }

    private static List<String> parseGenerics(char ch, PrimitiveIterator.OfInt it) {
        final List<String> generics = new ArrayList<>();
        if (ch == '<') {
            StringBuilder generic = new StringBuilder();
            int brackets = 1;
            while (true) {
                ch = (char) it.nextInt();
                if (ch == '>') brackets--;
                if (ch == '<') brackets++;
                if (brackets == 1 && ch == ',' || brackets == 0) {
                    if (generic.length() == 0) throw new Parser.ParseException("Expected 'type name' or '>'", null);
                    generics.add(generic.toString());
                    generic = new StringBuilder();
                } else {
                    generic.append(ch);
                }
                if (brackets == 0) break;
            }
            if (generic.length() != 0) throw new Parser.ParseException("Expected ';' before '>'", null);
        }
        return generics;
    }

    public static AsmType parseType(String name) {
        final char first = name.charAt(0);
        if (first == 'L') {
            return Parser.parseClass(name);
        } else if (first == '[') {
            return new AsmArray(Parser.parseType(name.substring(1, name.length())));
        } else if (AsmPrimitiveType.isPrimitive(name)) {
            return new AsmPrimitiveType(name);
        } if (AsmPrimitiveType.isPrimitive(first)) {
            return new AsmPrimitiveType(first);
        } else {
            throw new ParseException("Expected the target symbol in begin word", name);
        }
    }

    public static AsmClass parseClass(String full_name) {
        if (!(full_name.length() > 2 && full_name.charAt(0) == 'L' && full_name.charAt(full_name.length() - 1) == ';')) {
            throw new ParseException("Expected the symbol 'L' in begin of name and ';' in end", full_name);
        }
        full_name = full_name.substring(1, full_name.length() - 1);
        final StringBuilder name = new StringBuilder();
        final StringBuilder generics = new StringBuilder();
        final PrimitiveIterator.OfInt it = full_name.chars().iterator();
        int brackets = 0;
        while (it.hasNext()) {
            char ch = (char) it.nextInt();
            if (ch == '>') {
                brackets--;
                if (brackets == 0) break;
            }
            if (ch == '<') {
                brackets++;
                if (brackets == 0) continue;
            }
            if (brackets == 0) {
                name.append(ch);
            } else {
                generics.append(ch);
            }
        }
        if (brackets > 0) throw new ParseException("Expected '>' before 'eof'", full_name);
        if (it.hasNext()) throw new ParseException("Expected 'eof'", full_name);
        final List<String> temp = new ArrayList<>(Arrays.asList(name.toString().replace("/", ".").split("\\.")));
        final List<String> path = new ArrayList<>(temp.subList(0, temp.size() - 1));
        final List<String> names = new ArrayList<>(Arrays.asList(temp.get(temp.size() - 1).split("\\$")));
        if (names.size() == 0) throw new ParseException("Invalid full name", full_name);
        return new AsmClass(path, names);
    }

    public static MethodDescription parseMethod(String owner, String name, String desc) {
        return Parser.parseMethod(Parser.parseClass(owner), name, desc);
    }

    public static MethodDescription parseMethod(AsmClass owner, String name, String desc) {
        final Pair<List<String>, String> pair = Parser.parseDescription(desc);
        final AsmType type = Parser.parseType(pair.getValue1());
        final List<Pair<AsmType, String>> parameters = pair.getValue0().stream()
                .map(Parser::parseType)
                .map(t -> new Pair<>(t, ""))
                .collect(Collectors.toList());
        return new MethodDescription(name, owner, type, parameters);
    }

    public static MethodDescription parseDaikonMethodDescription(String daikonMethodDescription) {
        daikonMethodDescription = daikonMethodDescription.replace(" ", "");
        final Pattern pattern = Pattern.compile("(([\\w<>]+\\.)*)([\\w<>]+)\\(([\\w.<>,(\\[\\])]*)\\)");
        final Matcher matcher = pattern.matcher(daikonMethodDescription);
        if (!matcher.find())
            throw new ParseException("Daikon method description have wrong format", daikonMethodDescription);
        final String ownerFullName = String.format("L%s;", matcher.group(1).substring(0, matcher.group(1).length() - 1));
        final AsmClass owner = Parser.parseClass(ownerFullName);
        final String name = matcher.group(3).equals(owner.getName()) ? "<init>" : matcher.group(3);
        final List<String> arguments = new ArrayList<>();
        final PrimitiveIterator.OfInt it = matcher.group(4).chars().iterator();
        StringBuilder token = new StringBuilder();
        int brackets = 0;
        while (it.hasNext()) {
            char ch = (char) it.nextInt();
            if (ch == '>') brackets--;
            if (ch == '<') brackets++;
            token.append(ch);
            if (brackets == 0 && ch == ',') {
                if (token.length() == 0) throw new ParseException("Expected 'type name' or '>'", daikonMethodDescription);
                arguments.add(token.toString());
                token = new StringBuilder();
            }
        }
        if (token.length() == 0) {
            if (arguments.size() != 0) {
                throw new ParseException("Expected 'type name' or '>'", daikonMethodDescription);
            }
        } else {
            arguments.add(token.toString());
        }
        final List<Pair<AsmType, String>> parameters = arguments.stream()
                .map(type -> AsmPrimitiveType.isPrimitive(type) ? type : String.format("L%s;", type))
                .map(Parser::parseType)
                .map(t -> new Pair<>(t, ""))
                .collect(Collectors.toList());
        return new MethodDescription(name, owner, new AsmPrimitiveType("void"), parameters);
    }

    static class ParseException extends IllegalArgumentException {

        final String message;
        final String parsable;

        ParseException(String message, String parsable) {
            super(message + " :in " + (parsable == null ? "..." : parsable));
            this.message = message;
            this.parsable = parsable;
        }
    }
}
