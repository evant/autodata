package me.tatarka.autodata.compiler.plugins;

import com.google.auto.service.AutoService;
import com.google.common.collect.Maps;
import com.squareup.javapoet.*;
import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.plugins.AutoBuilder;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.beans.Introspector;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by evan on 4/21/15.
 */
@AutoService(AutoDataProcessor.class)
public class AutoBuilderProcessor implements AutoDataProcessor<AutoBuilder> {
    private Messager messager;
    private Types typeUtils;

    @Override
    public Class<AutoBuilder> forAnnotation() {
        return AutoBuilder.class;
    }

    @Override
    public void init(ProcessingEnvironment env) {
        messager = env.getMessager();
        typeUtils = env.getTypeUtils();
    }

    @Override
    public void process(AutoBuilder annotation, AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilder) {
        List<TypeElement> types = ElementFilter.typesIn(autoDataClass.getElement().getEnclosedElements());

        TypeElement builderClass = null;
        for (TypeElement type : types) {
            if (type.getAnnotation(AutoData.Builder.class) != null) {
                if (builderClass == null) {
                    builderClass = type;
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Cannot have multiple @AutoData.Builder's in " + autoDataClass.getElement().getQualifiedName() + "(" + builderClass.getQualifiedName() + " and " + type.getQualifiedName() + ")", type);
                }
            }
        }

        if (builderClass == null) {
            return;
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(builderClass.getSimpleName().toString())
                .addModifiers(Modifier.STATIC, Modifier.FINAL);

        if (builderClass.getKind() == ElementKind.INTERFACE) {
            builder.addSuperinterface(TypeName.get(builderClass.asType()));
        } else {
            builder.superclass(TypeName.get(builderClass.asType()));
        }

        Map<AutoDataField, ExecutableElement> setterMethodMap = Maps.newLinkedHashMap();
        List<ExecutableElement> builderMethods = ElementFilter.methodsIn(builderClass.getEnclosedElements());

        for (ExecutableElement method : builderMethods) {
            String fieldName = nameWithoutPrefix(method.getSimpleName().toString());
            for (AutoDataField field : autoDataClass.getFields()) {
                if (field.getName().equals(fieldName)) {
                    setterMethodMap.put(field, method);
                    break;
                }
            }
        }

        for (AutoDataField field : autoDataClass.getFields()) {
            TypeMirror fieldType = field.getType();
            if (fieldType instanceof PrimitiveType) {
                fieldType = typeUtils.boxedClass((PrimitiveType) fieldType).asType();
            }
            builder.addField(TypeName.get(fieldType), field.getName());
        }


        // constructors
        builder.addMethod(MethodSpec.constructorBuilder().build());

        {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addParameter(TypeName.get(autoDataClass.getElement().asType()), "source");

            for (Map.Entry<AutoDataField, ExecutableElement> entry : setterMethodMap.entrySet()) {
                AutoDataField field = entry.getKey();
                ExecutableElement method = entry.getValue();
                constructor.addStatement("$L(source.$L())", method.getSimpleName(), field.getGetterElement().getSimpleName());
            }

            builder.addMethod(constructor.build());
        }

        // setters
        for (Map.Entry<AutoDataField, ExecutableElement> entry : setterMethodMap.entrySet()) {
            AutoDataField field = entry.getKey();
            ExecutableElement method = entry.getValue();
            TypeName type = TypeName.get(field.getType());

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                    .addAnnotation(Override.class)
                    .addParameter(type, field.getName())
                    .returns(TypeName.get(builderClass.asType()));

            for (Modifier modifier : method.getModifiers()) {
                if (modifier == Modifier.ABSTRACT) {
                    continue;
                }
                methodBuilder.addModifiers(modifier);
            }

            if (type instanceof ArrayTypeName) {
                if (field.isNullable()) {
                    methodBuilder.addStatement("this.$L = ($L == null?) null : $L.clone()", field.getName(), field.getName(), field.getName());
                } else {
                    methodBuilder.addStatement("this.$L = $L.clone()", field.getName(), field.getName());
                }
            } else {
                methodBuilder.addStatement("this.$L = $L", field.getName(), field.getName());
            }

            methodBuilder.addStatement("return this");
        }

        // build
        {
            ExecutableElement buildMethod = null;
            for (ExecutableElement element : builderMethods) {
                if (!element.getParameters().isEmpty()) {
                    continue;
                }
                if (element.getReturnType().equals(autoDataClass.getElement().asType())) {
                    buildMethod = element;
                    break;
                }
            }

            if (buildMethod == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Builder class " + builderClass.getQualifiedName() + " in " + autoDataClass.getElement().getQualifiedName() + " must have a build method.", builderClass);
                return;
            }

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(buildMethod.getSimpleName().toString())
                    .addAnnotation(Override.class)
                    .returns(TypeName.get(buildMethod.getReturnType()));

            for (Modifier modifier : buildMethod.getModifiers()) {
                if (modifier == Modifier.ABSTRACT) {
                    continue;
                }
                methodBuilder.addModifiers(modifier);
            }

            boolean hasRequired = false;
            for (AutoDataField field : autoDataClass.getFields()) {
                if (!field.isNullable()) {
                    hasRequired = true;
                    break;
                }
            }

            if (hasRequired) {
                methodBuilder.addStatement("$T missing = \"\"", String.class);
                for (AutoDataField field : autoDataClass.getFields()) {
                    if (!field.isNullable()) {
                        methodBuilder.beginControlFlow("if ($L == null)", field.getName())
                                .addStatement("missing += \" $L\"", field.getName())
                                .endControlFlow();
                    }
                }
                methodBuilder.beginControlFlow("if(!missing.isEmpty())")
                        .addStatement("throw new $T(\"Missing required properties:\" + missing)", IllegalStateException.class)
                        .endControlFlow();
            }

            CodeBlock.Builder result = CodeBlock.builder()
                    .add("$T result = new $L(", TypeName.get(autoDataClass.getElement().asType()), autoDataClass.getGenClassName());
            for (Iterator<AutoDataField> iterator = autoDataClass.getFields().iterator(); iterator.hasNext(); ) {
                AutoDataField field = iterator.next();
                result.add("this.$L", field.getName());
                if (iterator.hasNext()) {
                    result.add(", ");
                }
            }
            result.add(");");

            methodBuilder.addCode(result.build());
            //TODO: validate

            methodBuilder.addStatement("return result");
            builder.addMethod(methodBuilder.build());
        }

        genClassBuilder.addType(builder.build());
    }

    private static String nameWithoutPrefix(String name) {
        if (name.startsWith("set")) {
            name = name.substring(3);
        }
        return Introspector.decapitalize(name);
    }
}
