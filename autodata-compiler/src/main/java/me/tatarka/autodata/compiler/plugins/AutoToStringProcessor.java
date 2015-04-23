package me.tatarka.autodata.compiler.plugins;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.plugins.AutoToString;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.util.Iterator;

/**
 * Created by evan on 4/21/15.
 */
@AutoService(AutoDataProcessor.class)
public class AutoToStringProcessor implements AutoDataProcessor<AutoToString> {
    @Override
    public Class<AutoToString> forAnnotation() {
        return AutoToString.class;
    }

    @Override
    public void init(ProcessingEnvironment env) {

    }

    @Override
    public void process(AutoToString annotation, AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilder) {
        // toString
        MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(String.class));


        CodeBlock.Builder block = CodeBlock.builder()
                .add("return \"$L{\"", autoDataClass.getElement().getSimpleName());
        for (Iterator<AutoDataField> iterator = autoDataClass.getFields().iterator(); iterator.hasNext(); ) {
            AutoDataField field = iterator.next();
            block.add(" + \"$L=\" + $L",
                    field.getName(),
                    TypeName.get(field.getType()) instanceof ArrayTypeName
                            ? "java.util.Arrays.toString(" + field.getName() + ")"
                            : field.getName());
            if (iterator.hasNext()) {
                block.add(" + \", \"");
            }
        }
        block.add(" + \"}\";");

        toString.addCode(block.build());
        genClassBuilder.addMethod(toString.build());
    }
}
