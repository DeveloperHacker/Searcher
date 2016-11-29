import analysers.AstVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String args[]) throws ClassNotFoundException, IOException, ParseException {
//        List<String> byteCodes = Arrays.asList("parts.AstMethod", "analysers.Parser", "analysers.MethodAnalyser");
//        Searcher searcher = new analysers.Searcher(null, byteCodes);
//        AstMethod method = Parser.parseMethod("Lanalysers.Parser;", "parseType", "(Ljava.lang.String;)Lparts.AstType;");
//        searcher.usages(method).forEach(System.out::println);
        String cwd = System.getProperty("user.dir");
        String javaFile = cwd + "/src/main/java/analysers/Parser.java";
        FileInputStream in = new FileInputStream(javaFile);
        CompilationUnit ast = JavaParser.parse(in);
        new AstVisitor().visit(ast, null);
    }
}

