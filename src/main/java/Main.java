import analysers.AstMethod;
import analysers.MethodDescription;
import analysers.Parser;
import analysers.Searcher;
import com.github.javaparser.ParseException;
import javassist.NotFoundException;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException, NotFoundException {
        final String cwd = System.getProperty("user.dir");
        final Set<String> byteCodes = new HashSet<>();
        byteCodes.add("analysers.MethodDescription");
        byteCodes.add("analysers.Parser");
        byteCodes.add("analysers.bytecode.AsmMethodAnalyser");
        final Searcher searcher = Searcher.normal(Searcher.searchCodes(cwd, "java"), byteCodes);
        final MethodDescription method = Parser.parseMethod("Lanalysers.Parser;", "parseDescription", "(CLjava.util.PrimitiveIterator$OfInt;)Lorg.javatuples.Pair;");
        final Pair<AstMethod, Set<AstMethod>> association = searcher.associate(method);
        System.out.println(association.getValue0());
        System.out.println("-------------------------------");
        association.getValue1().forEach(System.out::println);
    }
}

