package packers;

import analysers.analysable.AsmType;
import analysers.analysable.AstMethod;
import analysers.analysable.MethodDescription;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.collect.Sets;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public final class Packer {
    private static final String JAVA_DOC_TAG_NAME = "java-doc";
    private static final String HEAD_TAG_NAME = "@head";
    private static final String PARAM_TAG_NAME = "@param";
    private static final String RETURN_TAG_NAME = "@return";
    private static final String SEE_TAG_NAME = "@see";
    private static final String THROWS_TAG_NAME = "@throw";

    private static final String DESCRIPTION_TAG_NAME = "description";
    private static final String NAME_TAG_NAME = "name";
    private static final String TYPE_TAG_NAME = "type";
    private static final String PARAMETERS_TAG_NAME = "parameters";
    private static final String OWNER_TAG_NAME = "owner";

    private static final String CONTRACT_TAG_NAME = "contract";

    public static void pack(final String path, final Collection<AstMethod> methods) throws IOException {
        final JSONArray raw_methods = new JSONArray();
        for (AstMethod method : methods) {
            if (method.getJavadocComment() == null) continue;
            raw_methods.add(pack_method(method));
        }
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(raw_methods.toJSONString());
        }
    }

    private static JSONObject pack_method(final AstMethod method) {
        final JSONObject raw_method = new JSONObject();
        raw_method.put(JAVA_DOC_TAG_NAME, pack_java_doc(method.getJavadocComment()));
        raw_method.put(DESCRIPTION_TAG_NAME, pack_description(method.getDescription()));
        raw_method.put(CONTRACT_TAG_NAME, new JSONArray());
        return raw_method;
    }

    private static JSONObject pack_java_doc(final JavadocComment java_doc) {
        final JSONObject raw_java_doc = new JSONObject();
        final List<String> tokens = Arrays
                .stream(java_doc.getContent().split("(\n(\\s|\t)*/?\\*|\t|\\s)+"))
                .filter(token -> token.length() > 0)
                .collect(Collectors.toList());
        tokens.add(0, HEAD_TAG_NAME);
        final Set<String> tags = Sets.newHashSet(
                HEAD_TAG_NAME,
                PARAM_TAG_NAME,
                RETURN_TAG_NAME,
                SEE_TAG_NAME,
                THROWS_TAG_NAME
        );
        final Map<String, List<StringJoiner>> map = new HashMap<>();
        StringJoiner builder = null;
        for (String token : tokens) {
            if (tags.contains(token)) {
                if (!map.containsKey(token)) {
                    map.put(token, new LinkedList<>());
                }
                builder = new StringJoiner(" ");
                map.get(token).add(builder);
            }
            builder.add(token);
        }
        for (String tag : tags) {
            final JSONArray array = new JSONArray();
            array.addAll(map
                    .getOrDefault(tag, Collections.emptyList())
                    .stream()
                    .map(StringJoiner::toString)
                    .collect(Collectors.toList()));
            raw_java_doc.put(tag, array);
        }
        return raw_java_doc;
    }

    private static JSONObject pack_description(final MethodDescription description) {
        final JSONObject raw_description = new JSONObject();
        raw_description.put(NAME_TAG_NAME, description.getName());
        raw_description.put(TYPE_TAG_NAME, description.getType().getName());
        raw_description.put(OWNER_TAG_NAME, description.getOwner().getName());
        raw_description.put(PARAMETERS_TAG_NAME, pack_parameters(description.getParameters()));
        return raw_description;
    }

    private static JSONArray pack_parameters(final Collection<Pair<AsmType, String>> parameters) {
        final JSONArray raw_parameters = new JSONArray();
        for (Pair<AsmType, String> pair : parameters) {
            final String name = pair.getValue1();
            final AsmType type = pair.getValue0();
            final JSONObject raw_parameter = new JSONObject();
            raw_parameter.put(NAME_TAG_NAME, name);
            raw_parameter.put(TYPE_TAG_NAME, type.getName());
            raw_parameters.add(raw_parameter);
        }
        return raw_parameters;
    }
}
