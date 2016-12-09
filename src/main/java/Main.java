import analysers.AstMethod;
import analysers.MethodDescription;
import analysers.Parser;
import analysers.Searcher;
import com.github.javaparser.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException {
        String cwd = System.getProperty("user.dir");
        List<String> byteCodes = Arrays.asList(
                "analysers.MethodDescription",
                "analysers.Parser",
                "analysers.bytecode.AsmMethodAnalyser"
        );
        List<String> javaCodes = Stream.of(
                "/src/main/java/analysers/MethodDescription.java",
                "/src/main/java/analysers/Parser.java",
                "/src/main/java/analysers/bytecode/AsmMethodAnalyser.java"
        ).map(s -> cwd + s).collect(Collectors.toList());
        Searcher searcher = new analysers.Searcher(javaCodes, byteCodes);
        MethodDescription method = Parser.parseMethod("Lanalysers.Parser;", "parseType", "(Ljava.lang.String;)Lanalysers.bytecode.AsmType;");
        System.out.println();
        searcher.usages(method).stream().map(AstMethod::getDescription).forEach(System.out::println);
    }
}

