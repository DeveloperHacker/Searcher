import analysers.AstMethod;
import analysers.Searcher;
import com.github.javaparser.ast.comments.JavadocComment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Packer {

    private Packer() {

    }

    public static void packJavaDocs(String passToFolder, String fullFileName) throws IOException {
        final String javaDocBegin = "<JavaDoc>";
        final String javaDocEnd = "<JavaDoc\\>";
        final String methodDecryptionBegin = "<MethodDecryption>";
        final String methodDecryptionEnd = "<MethodDecryption\\>";
        final Searcher searcher = Searcher.simple(passToFolder);
        final Set<AstMethod> methods = searcher.getMethods();
        final PrintWriter writer = new PrintWriter(fullFileName);
        for (AstMethod method : methods) {
            final JavadocComment doc = method.getJavadocComment();
            if (doc == null) continue;
            final String comment = doc.getContent();
            final List<String> tokens = Arrays.stream(comment.split("(\n(\\s|\t)*\\*|\t|\\s)+"))
                    .filter(token -> token.length() > 0)
                    .collect(Collectors.toList());
            writer.println(javaDocBegin);
            writer.println(String.join(" ", tokens));
            writer.println(javaDocEnd);
            writer.println(method.getDescription().toString());
            writer.println();
        }
        writer.close();
    }
}
