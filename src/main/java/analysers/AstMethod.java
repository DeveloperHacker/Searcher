package analysers;

import com.github.javaparser.ast.comments.JavadocComment;

public class AstMethod {

    private final MethodDescription description;
    private final MethodParts methodParts;


    public AstMethod(MethodDescription description, MethodParts methodParts) {
        this.description = description;
        this.methodParts = methodParts;
    }

    public MethodDescription getDescription() {
        return description;
    }

    public JavadocComment getJavadocComment() {
        return methodParts.getJavadocComment();
    }

    public String getBody() {
        return methodParts.getBody();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AstMethod)) return false;

        final AstMethod astMethod = (AstMethod) o;

        return description.equals(astMethod.description);
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @Override
    public String toString() {
        final JavadocComment javadocComment = methodParts.getJavadocComment();
        return "\t/**" + (javadocComment == null ? "" : javadocComment.getContent()) + "*/\n" + description.getDescription() + " " + getBody();
    }
}
