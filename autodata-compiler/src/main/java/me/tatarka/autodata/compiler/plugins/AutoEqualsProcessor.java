package me.tatarka.autodata.compiler.plugins;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.plugins.AutoEquals;

/**
 * Created by evan on 4/20/15.
 */
@AutoService(AutoDataProcessor.class)
public class AutoEqualsProcessor implements AutoDataProcessor<AutoEquals> {
    @Override
    public Class<AutoEquals> forAnnotation() {
        return AutoEquals.class;
    }

    @Override
    public void process(AutoEquals annotation, final AutoDataClass autoDataClass, TypeSpec.Builder genClassBuilder) {
        // equals
        {
            MethodSpec.Builder equals = MethodSpec.methodBuilder("equals")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(TypeName.OBJECT, "o")
                    .returns(TypeName.BOOLEAN);

            CodeBlock.Builder block = CodeBlock.builder()
                    .beginControlFlow("if (o == this)")
                    .addStatement("return true")
                    .endControlFlow()
                    .beginControlFlow("if (o instanceof $T)", autoDataClass.getType());

            if (autoDataClass.getFields().isEmpty()) {
                block.addStatement("return true");
            } else {
                block.addStatement("$T that = ($T) o", autoDataClass.getType(), autoDataClass.getType());
                block.addStatement("return\n" + Joiner.on("\n&&").join(Iterables.transform(autoDataClass.getFields(), new Function<AutoDataField, String>() {
                    @Override
                    public String apply(AutoDataField input) {
                        return equalsExpr(autoDataClass, input);
                    }
                })));
            }
            block.endControlFlow();
            block.addStatement("return false");

            equals.addCode(block.build());
            genClassBuilder.addMethod(equals.build());
        }

        // hashCode
        {
            MethodSpec.Builder hashcode = MethodSpec.methodBuilder("hashCode")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(TypeName.INT);

            CodeBlock.Builder block = CodeBlock.builder()
                    .addStatement("int h = 1");

            for (AutoDataField field : autoDataClass.getFields()) {
                block.addStatement("h *= 1000003");
                block.addStatement("h ^= " + hashcodeExpr(field));
            }

            block.addStatement("return h");

            hashcode.addCode(block.build());
            genClassBuilder.addMethod(hashcode.build());
        }
    }

    private static String equalsExpr(AutoDataClass autoDataClass, AutoDataField field) {
        String thisName = "this." + field.getName();
        String thatName = "that." + autoDataClass.getGetterMethodForField(field).getName() + "()";
        TypeName type = field.getType();
        
        if (type == TypeName.FLOAT) {
            return "Float.floatToIntBits(" + thisName + ") == Float.floatToIntBits(" + thatName + ")";
        } else if (type == TypeName.DOUBLE) {
            return "Double.doubleToLongBits(" + thisName + ") == Double.doubleToLongBits(" + thatName + ")";
        } else if (type.isPrimitive()) {
            return thisName + " == " + thatName;
        } else if (type instanceof ArrayTypeName) {
            return "java.util.Arrays.equals(" + thisName + ", " + thatName + ")";
        } else if (field.isNullable()) {
            return "(" + thisName + " == null) ? (" + thatName + " == null) : " + thisName + ".equals(" + thatName + ")";
        } else {
            return thisName + ".equals(" + thatName + ")";
        }
    }

    private static String hashcodeExpr(AutoDataField field) {
        String name = field.getName();
        TypeName type = field.getType();

        if (type == TypeName.BYTE || type == TypeName.SHORT || type == TypeName.CHAR || type == TypeName.INT) {
            return name;
        } else if (type == TypeName.LONG) {
            return "(" + name + " >>> 32) ^ " + name;
        } else if (type == TypeName.FLOAT) {
            return "Float.floatToIntBits(" + name + ")";
        } else if (type == TypeName.DOUBLE) {
            return "Double.doubleToLongBits(" + name + ") >>> 32) ^ Double.doubleToLongBits(" + name + ")";
        } else if (type == TypeName.BOOLEAN) {
            return name + " ? 1231 : 1237";
        } else if (type instanceof ArrayTypeName) {
            return "java.util.Arrays.hashcode(" + name + ")";
        } else {
            return (field.isNullable() ? "(" + name + " == null) ? 0 : " : "") + name + ".hashcode()";
        }
    }
}
