import analysers.AstMethod;
import analysers.DaikonMethod;
import analysers.Parser;
import analysers.Searcher;
import org.javatuples.Pair;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


class Main {

    public static void main(String args[]) throws IOException, InterruptedException, XMLStreamException {
        if (args.length != 3) throw new IllegalArgumentException();
        final String daikonPath = args[0];
        final String inputPath = args[1];
        final String outputPath = args[2];
        final List<Pair<AstMethod, DaikonMethod>> methods = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] pair = line.split(":::");
                if (pair.length != 2) throw new IllegalArgumentException();
                final String path = pair[0];
                final String classPath = pair[1];
                final String className = Parser.parseClass(String.format("L%s;", classPath)).getName();
                final String dtrace = path + "/" + className + ".dtrace";
                final List<String> dyncomp = Arrays.asList("java", "-cp", ".:" + daikonPath, "daikon.DynComp", "--no-cset-file", classPath);
                final List<String> chicory = Arrays.asList("java", "-cp", ".:" + daikonPath, "daikon.Chicory", "--daikon", "--comparability-file=" + className + ".decls-DynComp", classPath);
                new ProcessBuilder(dyncomp).inheritIO().directory(new File(path)).start().waitFor();
                new ProcessBuilder(chicory).inheritIO().redirectOutput(new File(dtrace)).directory(new File(path)).start().waitFor();
                final Searcher searcher = Searcher.simple(path);
                searcher.update(Unpacker.unpack(dtrace));
                methods.addAll(searcher.getMethods());
            }
        }
        Packer.packMethods(outputPath + "/methods.xml", methods);
    }
}

