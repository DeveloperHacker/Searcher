package analysers;

import com.github.javaparser.ast.comments.JavadocComment;

public class MethodParts {

    private final JavadocComment javadocComment;
    private final String body;

    public MethodParts(JavadocComment javadocComment, String body) {
        this.javadocComment = javadocComment;
        this.body = body;
    }

    public JavadocComment getJavadocComment() {
        return javadocComment;
    }

    public String getBody() {
        return body;
    }
}
