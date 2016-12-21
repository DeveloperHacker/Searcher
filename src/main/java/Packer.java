import analysers.AstMethod;
import analysers.Searcher;
import analysers.bytecode.AsmType;
import com.github.javaparser.ast.comments.JavadocComment;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public final class Packer {

    private Packer() {

    }

    private static final String methodsTagName = "methods";
    private static final String methodTagName = "method";
    private static final String javaDocTagName = "javaDoc";
    private static final String headTagName = "head";
    private static final String paramTagName = "param";
    private static final String returnTagName = "return";
    private static final String seeTagName = "see";
    private static final String descriptionTagName = "description";
    private static final String nameTagName = "name";
    private static final String typeTagName = "type";
    private static final String parametersTagName = "parameters";
    private static final String ownerTagName = "owner";

    public static void packJavaDocs(String passToFolder, String passToFile, String fileName) throws IOException, XMLStreamException {

        final Searcher searcher = Searcher.simple(passToFolder);
        final Set<AstMethod> methods = searcher.getMethods();
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = output.createXMLStreamWriter(new FileWriter(passToFile + "/" + fileName + ".xml"));
        writer.writeStartDocument();
        writer.writeStartElement(methodsTagName);
        for (AstMethod method : methods) {
            final JavadocComment doc = method.getJavadocComment();
            if (doc == null) continue;
            final String[] tokens = doc.getContent().split("(\n(\\s|\t)*\\*|\t|\\s)+");
            writer.writeStartElement(methodTagName);
            writer.writeStartElement(javaDocTagName);

            writer.writeStartElement(headTagName);
            for (String token : tokens) {
                switch (token) {
                    case "@return" : {
                        writer.writeEndElement();
                        writer.writeStartElement(returnTagName);
                        break;
                    }
                    case "@param" : {
                        writer.writeEndElement();
                        writer.writeStartElement(paramTagName);
                        break;
                    }
                    case "@see" : {
                        writer.writeEndElement();
                        writer.writeStartElement(seeTagName);
                        break;
                    }
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
            for (AsmType type : method.getDescription().getParameters()) {
                writer.writeStartElement(typeTagName);
                writer.writeCharacters(type.getName());
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
}
