package analysers.analysable;

import com.github.javaparser.ast.comments.JavadocComment;
import utils.Shell;

public class AstMethod extends Shell<MethodDescription> {

    private final MethodDescription description;
    private final JavadocComment javadocComment;
    private final String body;


    public AstMethod(MethodDescription description, JavadocComment javadocComment, String body) {
        super(description);
        this.description = description;
        this.javadocComment = javadocComment;
        this.body = body;
    }

    public MethodDescription getDescription() {
        return this.description;
    }

    public JavadocComment getJavadocComment() {
        return javadocComment;
    }

    private String getBody() {
        return body;
    }

    @Override
    public String toString() {
        final JavadocComment doc = this.javadocComment;
        return "\t/**" + (doc == null ? "" : doc.getContent()) + "*/\n" + this.description.toString() + " " + getBody();
    }
}
