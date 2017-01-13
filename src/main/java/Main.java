import analysers.DaikonMethod;
import com.github.javaparser.ParseException;
import javassist.NotFoundException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException, NotFoundException, XMLStreamException {
//        final String cwd = System.getProperty("user.dir");
//        String passToFolder = cwd;
//        String passToFile = cwd;
//        String fileName = "pack";
//        switch (args.length) {
//            case 3: {
//                fileName = args[2];
//            }
//            case 2: {
//                passToFile = args[1];
//            }
//            case 1: {
//                passToFolder = args[0];
//            }
//        }
//        final Searcher searcher = Searcher.simple(passToFolder);
//        final Set<AstMethod> methods = searcher.getMethods();
//        Packer.pack(passToFile + "/" + fileName, methods);
        final String path = "/home/vorobyev/ProgramFiles/daikon-5.4.6/examples/java-examples/QueueAr/QueueArTester.dtrace.txt";
        final Set<DaikonMethod> unpacked = Unpacker.unpack(path);
        unpacked.stream().filter(daikonMethod -> !daikonMethod.enter.isEmpty()).forEach(System.out::println);
    }
}

