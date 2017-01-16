import analysers.AstMethod;
import analysers.DaikonMethod;
import analysers.Searcher;
import com.github.javaparser.ParseException;
import javassist.NotFoundException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException, NotFoundException, XMLStreamException {
        final String passToInput = args[1];
        final String passToOutput = args[2];
        if ("ast".equals(args[0])) {
            final Searcher searcher = Searcher.simple(passToInput);
            final Set<AstMethod> methods = searcher.getMethods();
            Packer.packAstMethods(passToOutput, methods);
        } else if ("daikon".equals(args[0])) {
            final Set<DaikonMethod> methods = Unpacker.unpack(passToInput);
            Packer.packDaikonMethods(passToOutput, methods);
        }
    }
}

