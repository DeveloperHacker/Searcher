package packers;

import analysers.analysable.AsmType;
import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import analysers.analysable.MethodDescription;
import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Packer {

    private static final String METHODS_TAG_NAME = "methods";
    private static final String METHOD_TAG_NAME = "method";

    private static final String JAVA_DOC_TAG_NAME = "javaDoc";
    private static final String HEAD_TAG_NAME = "head";
    private static final String PARAM_TAG_NAME = "param";
    private static final String RETURN_TAG_NAME = "return";
    private static final String SEE_TAG_NAME = "see";
    private static final String THROWS_TAG_NAME = "throws";

    private static final String DESCRIPTION_TAG_NAME = "description";
    private static final String NAME_TAG_NAME = "name";
    private static final String TYPE_TAG_NAME = "type";
    private static final String PARAMETERS_TAG_NAME = "parameters";
    private static final String OWNER_TAG_NAME = "owner";

    private static final String CONTRACT_TAG_NAME = "contract";
    private static final String INV_CONDITION_TAG_NAME = "inv-condition";
    private static final String PRE_CONDITION_TAG_NAME = "pre-condition";
    private static final String POST_CONDITION_TAG_NAME = "post-condition";

    private static final String DAIKON_CONTRACT_TAG_NAME = "daikon-contract";
    private static final String ENTER_TAG_NAME = "enter";
    private static final String ENTERS_TAG_NAME = "enters";
    private static final String EXIT_TAG_NAME = "exit";
    private static final String EXITS_TAG_NAME = "exits";
    private static final String EXIT_ID_TAG_NAME = "exitId";
    private static final String EXIT_IDS_TAG_NAME = "exitIds";

    public static void pack(
            final String file_name,
            final Collection<Pair<AstMethod, DaikonMethod>> methods
    ) throws IOException, XMLStreamException {
        System.out.println(methods.size());
        final XMLOutputFactory output = XMLOutputFactory.newInstance();
        final XMLStreamWriter writer = output.createXMLStreamWriter(new FileWriter(file_name));
        writer.writeStartDocument();
        writer.writeStartElement(METHODS_TAG_NAME);
        for (Pair<AstMethod, DaikonMethod> method : methods) {
            final AstMethod ast_method = method.getValue0();
            if (ast_method == null) continue;
            final JavadocComment javadoc = ast_method.getJavadocComment();
            if (javadoc == null) continue;
            final DaikonMethod daikon_method = method.getValue1();
            final MethodDescription description = ast_method.getDescription();
            writer.writeStartElement(METHOD_TAG_NAME);
            pack_javadoc(writer, javadoc);
            pack_description(writer, description);
            pack_contract_template(writer);
            if (daikon_method != null) pack_daikon_contract(writer, daikon_method);
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.close();
    }


    private static void pack_contract_template(final XMLStreamWriter writer) throws IOException, XMLStreamException {
        writer.writeStartElement(CONTRACT_TAG_NAME);
        writer.writeStartElement(INV_CONDITION_TAG_NAME);
        writer.writeEndElement();
        writer.writeStartElement(PRE_CONDITION_TAG_NAME);
        writer.writeEndElement();
        writer.writeStartElement(POST_CONDITION_TAG_NAME);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void pack_javadoc(
            final XMLStreamWriter writer,
            final JavadocComment javadoc
    ) throws XMLStreamException {
        final List<String> tokens = Arrays.stream(javadoc.getContent().split("(\n(\\s|\t)*/?\\*|\t|\\s)+"))
                .filter(token -> token.length() > 0)
                .collect(Collectors.toList());
        writer.writeStartElement(JAVA_DOC_TAG_NAME);
        writer.writeStartElement(HEAD_TAG_NAME);
        for (String token : tokens) {
            final String tag;
            switch (token) {
                case "@return":
                    tag = RETURN_TAG_NAME;
                    break;
                case "@param":
                    tag = PARAM_TAG_NAME;
                    break;
                case "@see":
                    tag = SEE_TAG_NAME;
                    break;
                case "@throws":
                    tag = THROWS_TAG_NAME;
                    break;
                default:
                    tag = null;
            }
            if (tag != null) {
                writer.writeEndElement();
                writer.writeStartElement(tag);
            }
            writer.writeCharacters(token + " ");
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void pack_description(
            final XMLStreamWriter writer,
            final MethodDescription description
    ) throws XMLStreamException {
        writer.writeStartElement(DESCRIPTION_TAG_NAME);
        writer.writeStartElement(NAME_TAG_NAME);
        writer.writeCharacters(description.getName());
        writer.writeEndElement();
        writer.writeStartElement(TYPE_TAG_NAME);
        writer.writeCharacters(description.getType().getName());
        writer.writeEndElement();
        writer.writeStartElement(PARAMETERS_TAG_NAME);
        for (Pair<AsmType, String> type : description.getParameters()) {
            writer.writeStartElement(PARAM_TAG_NAME);
            writer.writeStartElement(TYPE_TAG_NAME);
            writer.writeCharacters(type.getValue0().getName());
            writer.writeEndElement();
            writer.writeStartElement(NAME_TAG_NAME);
            writer.writeCharacters(type.getValue1());
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeStartElement(OWNER_TAG_NAME);
        writer.writeCharacters(description.getOwner().getName());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void pack_daikon_contract(
            final XMLStreamWriter writer,
            final DaikonMethod method
    ) throws XMLStreamException {
        writer.writeStartElement(DAIKON_CONTRACT_TAG_NAME);
        writer.writeStartElement(ENTERS_TAG_NAME);
        for (String enter : method.enter) {
            writer.writeStartElement(ENTER_TAG_NAME);
            writer.writeCharacters(enter);
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeStartElement(EXITS_TAG_NAME);
        for (String exit : method.exit) {
            writer.writeStartElement(EXIT_TAG_NAME);
            writer.writeCharacters(exit);
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeStartElement(EXIT_IDS_TAG_NAME);
        for (Map.Entry<Integer, Set<String>> entry : method.exits.entrySet()) {
            writer.writeStartElement(EXIT_ID_TAG_NAME);
            writer.writeCharacters(entry.getKey().toString());
            writer.writeEndElement();
            writer.writeStartElement(EXITS_TAG_NAME);
            for (String exit : entry.getValue()) {
                writer.writeStartElement(EXIT_TAG_NAME);
                writer.writeCharacters(exit);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
