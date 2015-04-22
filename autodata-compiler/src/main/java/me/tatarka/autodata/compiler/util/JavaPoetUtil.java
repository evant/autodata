package me.tatarka.autodata.compiler.util;

import com.squareup.javapoet.TypeName;

/**
 * Created by evan on 4/21/15.
 */
public final class JavaPoetUtil {
    private JavaPoetUtil() {
    }

    /**
     * Returns the name of the given type without the package.
     */
    public static String getSimpleTypeName(TypeName typeName) {
        String name = typeName.toString();
        if (!name.contains(".")) {
            return name;
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * Returns a TypeName that represents the boxed type for the given primative type. If it is not
     * a primitive type, returns the given TypeName.
     */
    public static TypeName boxType(TypeName typeName) {
        if (!typeName.isPrimitive()) {
            return typeName;
        }

        if (typeName == TypeName.VOID) {
            return TypeName.get(Void.class);
        }
        if (typeName == TypeName.BOOLEAN) {
            return TypeName.get(Boolean.class);
        }
        if (typeName == TypeName.BYTE) {
            return TypeName.get(Byte.class);
        }
        if (typeName == TypeName.SHORT) {
            return TypeName.get(Short.class);
        }
        if (typeName == TypeName.INT) {
            return TypeName.get(Integer.class);
        }
        if (typeName == TypeName.LONG) {
            return TypeName.get(Long.class);
        }
        if (typeName == TypeName.CHAR) {
            return TypeName.get(Character.class);
        }
        if (typeName == TypeName.FLOAT) {
            return TypeName.get(Float.class);
        }
        if (typeName == TypeName.DOUBLE) {
            return TypeName.get(Double.class);
        }

        // Shouldn't be reachable. 
        throw new IllegalArgumentException("Unknown TypeName: " + typeName);
    }
}
