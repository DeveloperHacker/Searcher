import com.github.javaparser.ParseException
import fast_search.mine
import fast_search.pack
import java.io.IOException
import javax.xml.stream.XMLStreamException

@Throws(IOException::class, ParseException::class, XMLStreamException::class)
fun main(args: Array<String>) {
    val pwd = System.getProperty("user.dir")
    val arguments = args
            .map { it.split("=".toRegex()).toList() }
            .map { it[0] to it.drop(1).joinToString("=") }.toMap()
    val input_path = arguments.getOrDefault("--input", pwd)
    val output_path = arguments.getOrDefault("--output", pwd + "/methods.json")
    val methods = mine(input_path)
    pack(output_path, methods)
    //        final Collection<AstMethod> methods = Miner.mine(input_path);
    //        Packer.pack(output_path, methods);
}

