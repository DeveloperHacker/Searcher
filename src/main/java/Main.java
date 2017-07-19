import analysers.Miner;
import analysers.analysable.AstMethod;
import com.github.javaparser.ParseException;
import packers.Packer;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Main {

    public static void main(final String args[]) throws IOException, ParseException, XMLStreamException {
        final String pwd = System.getProperty("user.dir");
        final Map<String, String> arguments = Main.arguments(args);
        final String input_path = arguments.getOrDefault("--input", pwd);
        final String output_path = arguments.getOrDefault("--output", pwd + "/methods.json");
        final Collection<AstMethod> methods = Miner.mine(input_path);
        Packer.pack(output_path, methods);
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
