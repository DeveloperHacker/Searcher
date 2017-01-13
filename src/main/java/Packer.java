import analysers.AstMethod;
import analysers.bytecode.AsmType;
import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class Packer {

    private Packer() {

    }

    private static final String methodsTagName = "methods";
    private static final String methodTagName = "method";
    private static final String javaDocTagName = "javaDoc";
    private static final String headTagName = "head";
    private static final String paramTagName = "param";
    private static final String returnTagName = "return";
    private static final String seeTagName = "see";
    private static final String throwsTagName = "throws";
    private static final String descriptionTagName = "description";
    private static final String nameTagName = "name";
    private static final String typeTagName = "type";
    private static final String parametersTagName = "parameters";
    private static final String ownerTagName = "owner";

    public static void pack(String fileName, Set<AstMethod> methods) throws IOException, XMLStreamException {
        final XMLOutputFactory output = XMLOutputFactory.newInstance();
        final XMLStreamWriter writer = output.createXMLStreamWriter(new FileWriter(fileName + ".xml"));
        writer.writeStartDocument();
        writer.writeStartElement(methodsTagName);
        for (AstMethod method : methods) {
            final JavadocComment doc = method.getJavadocComment();
            if (doc == null) continue;
            final List<String> tokens = Arrays.stream(doc.getContent().split("(\n(\\s|\t)*/?\\*|\t|\\s)+"))
                    .filter(token -> token.length() > 0)
                    .collect(Collectors.toList());
            if (tokens.size() == 0 || tokens.get(0).length() == 0) continue;
            writer.writeStartElement(methodTagName);
            writer.writeStartElement(javaDocTagName);
            writer.writeStartElement(Packer.getTag(tokens.get(0)));
            writer.writeCharacters(tokens.get(0) + " ");
            for (String token : tokens.subList(1, tokens.size())) {
                String tag = Packer.getTag(token);
                if (!tag.equals(headTagName)) {
                    writer.writeEndElement();
                    writer.writeStartElement(tag);
                }
                writer.writeCharacters(token + " ");
            }
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeStartElement(descriptionTagName);
            writer.writeStartElement(nameTagName);
            writer.writeCharacters(method.getDescription().getName());
            writer.writeEndElement();
            writer.writeStartElement(typeTagName);
            writer.writeCharacters(method.getDescription().getType().getName());
            writer.writeEndElement();
            writer.writeStartElement(parametersTagName);
            for (Pair<AsmType, String> type : method.getDescription().getParameters()) {
                writer.writeStartElement(paramTagName);
                writer.writeStartElement(typeTagName);
                writer.writeCharacters(type.getValue0().getName());
                writer.writeEndElement();
                writer.writeStartElement(nameTagName);
                writer.writeCharacters(type.getValue1());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeStartElement(ownerTagName);
            writer.writeCharacters(method.getDescription().getOwner().getName());
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.close();
    }

    private static String getTag(String token) {
        switch (token) {
            case "@return": return returnTagName;
            case "@param": return paramTagName;
            case "@see": return seeTagName;
            case "@throws": return throwsTagName;
            default: return headTagName;
        }
    }
}
