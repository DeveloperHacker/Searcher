package parts;

import java.util.HashMap;
import java.util.Map;

public class AstPrimitiveType extends AstType {
    private final static Map<String, String> types;

    static {
        types = new HashMap<>();
        types.put("B", "byte");
        types.put("C", "char");
        types.put("D", "double");
        types.put("F", "float");
        types.put("I", "int");
        types.put("J", "long");
        types.put("S", "short");
        types.put("Z", "boolean");
        types.put("V", "void");
        types.put("byte", "byte");
        types.put("char", "char");
        types.put("double", "double");
        types.put("float", "float");
        types.put("int", "int");
        types.put("long", "long");
        types.put("short", "short");
        types.put("boolean", "boolean");
        types.put("void", "void");
    }

    public AstPrimitiveType(String name) {
        super(types.get(name));
    }

    public static boolean isPrimitive(String type) {
        return type.length() == 1 && types.containsKey(type);
    }

    public static boolean isPrimitive(Character type) {
        return types.containsKey(type.toString());
    }

}
