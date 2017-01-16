package analysers.bytecode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class AsmPrimitiveType extends AsmType {
    private final static BiMap<String, Character> types;

    static {
        types = HashBiMap.create();
        types.put("byte", 'B');
        types.put("char", 'C');
        types.put("double", 'D');
        types.put("float", 'F');
        types.put("int", 'I');
        types.put("long", 'J');
        types.put("short", 'S');
        types.put("boolean", 'Z');
        types.put("void", 'V');
    }

    public static Character shortRepresentation(String string) {
        return types.get(string);
    }

    public AsmPrimitiveType(Character name) {
        super(validate(name));
    }

    public AsmPrimitiveType(String name) {
        super(validate(name));
    }

    private static String validate(Character name) {
        if (!AsmPrimitiveType.isPrimitive(name)) {
            throw new IllegalArgumentException();
        }
        return name.toString();
    }

    private static String validate(String name) {
        if (!AsmPrimitiveType.isPrimitive(name)) {
            throw new IllegalArgumentException();
        }
        return types.get(name).toString();
    }

    public static boolean isPrimitive(String type) {
        return types.containsKey(type);
    }

    public static boolean isPrimitive(Character type) {
        return types.inverse().containsKey(type);
    }
}
