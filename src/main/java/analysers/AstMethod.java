package analysers;

import com.github.javaparser.ast.comments.JavadocComment;
import utils.Shell;

public class AstMethod implements Shell<MethodDescription> {

    private final MethodDescription description;
    private final MethodParts methodParts;


    public AstMethod(MethodDescription description, MethodParts methodParts) {
        this.description = description;
        this.methodParts = methodParts;
    }

    public MethodDescription getDescription() {
        return this.description;
    }

    public JavadocComment getJavadocComment() {
        return this.methodParts.getJavadocComment();
    }

    private String getBody() {
        return this.methodParts.getBody();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AstMethod)) return false;

        final AstMethod astMethod = (AstMethod) o;

        return this.description.equals(astMethod.description);
    }

    @Override
    public int hashCode() {
        return this.description.hashCode();
    }

    @Override
    public String toString() {
        final JavadocComment javadocComment = this.methodParts.getJavadocComment();
        return "\t/**" + (javadocComment == null ? "" : javadocComment.getContent()) + "*/\n" + this.description.getDescription() + " " + getBody();
    }

    @Override
    public MethodDescription inner() {
        return this.description;
    }
}
