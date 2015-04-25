package me.tatarka.autodata.compiler.plugins;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.plugins.AutoEquals;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by evan on 4/20/15.
 */
@AutoService(AutoDataProcessor.class)
public class AutoEqualsProcessor implements AutoDataProcessor<AutoEquals> {
    @Override
    public void init(ProcessingEnvironment env) {

    }

    @Override
    public void process(AutoEquals annotation, final AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilder) {
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
                    .beginControlFlow("if (o instanceof $T)", autoDataClass.getElement());

            if (autoDataClass.getFields().isEmpty()) {
                block.addStatement("return true");
            } else {
                block.addStatement("$T that = ($T) o", autoDataClass.getElement(), autoDataClass.getElement());

                block.add("return ");
                for (Iterator<AutoDataField> iterator = autoDataClass.getFields().iterator(); iterator.hasNext(); ) {
                    AutoDataField field = iterator.next();
                    block.add(equalsExpr(field));
                    if (iterator.hasNext()) {
                        block.add(" && ");
                    }
                }
                block.add(";");
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

    private static CodeBlock equalsExpr(AutoDataField field) {
        CodeBlock.Builder block = CodeBlock.builder();

        String thisName = "this." + field.getName();
        String thatName = "that." + field.getGetterMethod().getName() + "()";
        TypeName type = TypeName.get(field.getType());
        
        if (type == TypeName.FLOAT) {
            block.add("Float.floatToIntBits($L) == Float.floatToIntBits($L)", thisName, thatName);
        } else if (type == TypeName.DOUBLE) {
            block.add("Double.doubleToLongBits($L) == Double.doubleToLongBits($L)", thisName, thatName);
        } else if (type.isPrimitive()) {
            block.add("$L == $L", thisName, thatName);
        } else if (type instanceof ArrayTypeName) {
            block.add("$T.equals($L, $L)", Arrays.class, thisName, thatName);
        } else if (field.isNullable()) {
            block.add("($L == null) ? ($L == null) : $L.equals($L)", thisName, thatName, thisName, thatName);
        } else {
            block.add("$L.equals($L)", thisName, thatName);
        }

        return block.build();
    }

    private static CodeBlock hashcodeExpr(AutoDataField field) {
        CodeBlock.Builder block = CodeBlock.builder();

        String name = field.getName();
        TypeName type = TypeName.get(field.getType());

        if (type == TypeName.BYTE || type == TypeName.SHORT || type == TypeName.CHAR || type == TypeName.INT) {
            block.add(name);
        } else if (type == TypeName.LONG) {
            block.add("($L >>> 32) ^ $L", name, name);
        } else if (type == TypeName.FLOAT) {
            block.add("Float.floatToIntBits($L)", name);
        } else if (type == TypeName.DOUBLE) {
            block.add("Double.doubleToLongBits($L) >>> 32) ^ Double.doubleToLongBits($L)", name, name);
        } else if (type == TypeName.BOOLEAN) {
            block.add("$L ? 1231 : 1237", name);
        } else if (type instanceof ArrayTypeName) {
            block.add("$T.hashCode($L)", Arrays.class, name);
        } else {
            if (field.isNullable()) {
                block.add("($L == null) ? 0 : ", name);
            }
            block.add("$L.hashCode()", name);
        }

        return block.build();
    }
}
