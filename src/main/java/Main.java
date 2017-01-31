import analysers.Miner;
import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import com.github.javaparser.ParseException;
import org.javatuples.Pair;
import packers.Packer;
import packers.Unpacker;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;


public class Main {

    public static void main(final String args[]) {
        try {
            final String pwd = System.getProperty("user.dir");
            final Map<String, String> arguments = Main.arguments(args);
            final String dir = arguments.containsKey("--dir") ? arguments.get("--dir") : pwd;
            final String dtrace = arguments.containsKey("--dtrace") ? arguments.get("--dtrace") : null;
            final String xml = arguments.containsKey("--xml") ? arguments.get("--xml") : pwd + "methods.xml";
            final Collection<AstMethod> astMethods = Miner.mine(dir);
            final Collection<DaikonMethod> daikonMethods = dtrace == null ? Collections.emptyList(): Unpacker.unpackSimple(dtrace);
            final Collection<Pair<AstMethod, DaikonMethod>> methods = Miner.associate(astMethods, daikonMethods);
            Packer.pack(xml, methods);
        } catch (AssertionError | IOException | XMLStreamException | ParseException e) {
            System.out.println(e.getMessage());
        }
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
