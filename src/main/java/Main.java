import com.github.javaparser.ParseException;
import javassist.NotFoundException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException, NotFoundException, XMLStreamException {
        final String cwd = System.getProperty("user.dir");
////        final Set<String> byteCodes = new HashSet<>();
////        byteCodes.add("analysers.MethodDescription");
////        byteCodes.add("analysers.Parser");
////        byteCodes.add("analysers.bytecode.AsmMethodAnalyser");
////        final Searcher searcher = Searcher.normal(Searcher.loadJava(cwd), byteCodes);
//        final Searcher searcher = Searcher.simple(cwd);
////        final MethodDescription method = Parser.parseMethod("Lanalysers.Parser;", "parseDescription", "(CLjava.util.PrimitiveIterator$OfInt;)Lorg.javatuples.Pair;");
//        final MethodDescription method = Parser.parseMethod("LParser;", "parseDescription", "(CLOfInt;)LPair;");
//        final Pair<AstMethod, Set<AstMethod>> association = searcher.associate(method);
//        System.out.println(association.getValue0());
//        System.out.println("-------------------------------");
//        association.getValue1().forEach(System.out::println);
        Packer.packJavaDocs(cwd, cwd, "packJavaDocs");
    }
}

