package fast_search

import com.github.javaparser.JavaParser
import org.apache.maven.shared.utils.io.DirectoryScanner
import java.io.FileInputStream


private fun indexJavaCodes(javaCodes: Set<String>): Set<Method> {
    val visitor = Visitor()
    for (code in javaCodes) {
        println(code)
        val input = FileInputStream(code)
        val ast = JavaParser.parse(input)
        visitor.visit(ast, null)
    }
    println(visitor.methods.size)
    return visitor.methods
}

private fun loadJava(path: String): Set<String> {
    val scanner = DirectoryScanner()
    scanner.setIncludes("**/*.java")
    scanner.setBasedir(path)
    scanner.setCaseSensitive(false)
    scanner.scan()
    val file_names = scanner.includedFiles
    return file_names.map { path + "/" + it }.toSet()
}

fun mine(path: String): Collection<Method> {
    val javaCodes = loadJava(path)
    return indexJavaCodes(javaCodes)
}