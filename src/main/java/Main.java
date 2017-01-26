import analysers.Parser;
import analysers.Searcher;
import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import com.github.javaparser.ParseException;
import org.javatuples.Pair;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


class Main {

    public static void main(String args[]) throws IOException, InterruptedException, XMLStreamException, ParseException {
        final Map<String, String> arguments = Main.arguments(args);
        final String pwd = System.getProperty("user.dir");
        assert arguments.containsKey("--daikon");
        final String daikon = arguments.get("--daikon");
        final String config = arguments.containsKey("--config") ? arguments.get("--config") : (pwd + "/config.cfg");
        final String output = arguments.containsKey("--output") ? arguments.get("--output") : pwd;
        final List<Pair<AstMethod, DaikonMethod>> methods = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(config))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] pair = line.split(":::");
                assert pair.length == 2;
                final String path = pair[0];
                final String classPath = pair[1];
                final String className = Parser.parseClass(String.format("L%s;", classPath)).getName();
                final String dtrace = path + "/" + className + ".dtrace";
                final List<String> dyncomp = Arrays.asList("java", "-cp", ".:" + daikon + "/daikon.jar", "daikon.DynComp", "--no-cset-file", classPath);
                final List<String> chicory = Arrays.asList("java", "-cp", ".:" + daikon + "/daikon.jar", "daikon.Chicory", "--daikon", "--comparability-file=" + className + ".decls-DynComp", classPath);
                final ProcessBuilder processDyncomp = new ProcessBuilder(dyncomp);
                processDyncomp.environment().put("DAIKONDIR", daikon);
                processDyncomp.inheritIO().directory(new File(path)).start().waitFor();
                final ProcessBuilder processChicory = new ProcessBuilder(chicory);
                processChicory.environment().put("DAIKONDIR", daikon);
                processChicory.inheritIO().redirectOutput(new File(dtrace)).directory(new File(path)).start().waitFor();
                final Searcher searcher = Searcher.simple(path);
                searcher.update(Unpacker.unpackSimple(dtrace));
                methods.addAll(searcher.getMethods());
                Main.print(searcher.getMethods());
            }
        }
        Packer.packMethods(output + "/methods.xml", methods);
    }

    private static Map<String, String> arguments(String[] args) {
        final Map<String, String> result = new HashMap<>();
        for (String arg : args) {
            final String[] argArr = arg.split("=");
            final String name = argArr[0];
            final String value = argArr.length == 1 ? null : String.join("=", Arrays.copyOfRange(argArr, 1, argArr.length));
            result.put(name, value);
        }
        return result;
    }

    private static void print(Set<Pair<AstMethod, DaikonMethod>> methods) {
        final List<Pair<String, String>> lines = new ArrayList<>();
        int counter = 0;
        float average = 10;
        for (Pair<AstMethod, DaikonMethod> bound : methods) {
            final AstMethod astMethod = bound.getValue0();
            final DaikonMethod daikonMethod = bound.getValue1();
            final String astDescription = astMethod == null ? "..." : astMethod.getDescription().toString();
            final String daikonDescription = daikonMethod == null ? "..." : daikonMethod.getDescription().toString();
            if (astMethod == null || daikonMethod == null) {
                counter++;
            }
            lines.add(new Pair<>(astDescription, daikonDescription));
            average += (float) astDescription.length() / (float) methods.size();
        }
        final int length = Math.round(average);
        System.out.println();
        System.out.println(String.format(String.format("%%-%ds -> %%s", length), "AST", "DAIKON"));
        lines.stream().map(pair -> String.format(String.format("%%-%ds -> %%s", length), pair.getValue0(), pair.getValue1())).forEach(System.out::println);
        System.out.println(String.format("%d/%d", counter, methods.size()));
        System.out.println("=====================================================================");
        System.out.println();
    }
}

