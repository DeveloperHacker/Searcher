import com.github.javaparser.ParseException;
import javassist.NotFoundException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException, NotFoundException, XMLStreamException {
        final String cwd = System.getProperty("user.dir");
        String passToFolder = cwd;
        String passToFile = cwd;
        String fileName = "packJavaDocs";
        switch (args.length) {
            case 3: {
                fileName = args[2];
            }
            case 2: {
                passToFile = args[1];
            }
            case 1: {
                passToFolder = args[0];
            }
        }
        Packer.packJavaDocs(passToFolder, passToFile, fileName);
    }
}

