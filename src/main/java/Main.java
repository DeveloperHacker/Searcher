import analysers.Parser;
import analysers.Searcher;
import parts.Method;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException {
        List<String> byteCodes = Arrays.asList("parts.Method", "analysers.Parser", "analysers.MethodAnalyser");
        Searcher searcher = new analysers.Searcher(null, byteCodes);
        Method method = Parser.parseMethod("Lanalysers.Parser;", "parseType", "(Ljava.lang.String;)Lparts.Type;");
        searcher.usages(method).forEach(System.out::println);
    }
}