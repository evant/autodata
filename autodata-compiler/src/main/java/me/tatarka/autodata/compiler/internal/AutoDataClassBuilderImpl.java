package me.tatarka.autodata.compiler.internal;

import com.google.common.collect.Lists;
import com.squareup.javapoet.*;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;
import me.tatarka.autodata.compiler.model.AutoDataField;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by evan on 4/22/15.
 */
class AutoDataClassBuilderImpl implements AutoDataClassBuilder {
    private AutoDataClass autoDataClass;
    private List<ExecutableElement> declaredMethods;
    private Types typeUtils;
    private Messager messager;
    private String currentPluginName;
    private List<MethodSpec> addedMethods = Lists.newArrayList();

    final TypeSpec.Builder builder;

    AutoDataClassBuilderImpl(String className, AutoDataClass autoDataClass, List<ExecutableElement> declaredMethods, Messager messager, Types typeUtils) {
        builder = TypeSpec.classBuilder(className);
        this.autoDataClass = autoDataClass;
        this.declaredMethods = declaredMethods;
        this.messager = messager;
        this.typeUtils = typeUtils;
    }

    void setCurrentPluginName(String name) {
        currentPluginName = name;
    }

    @Override
    public AutoDataClassBuilderImpl addAnnotation(AnnotationSpec annotationSpec) {
        builder.addAnnotation(annotationSpec);
        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addAnnotation(ClassName annotation) {
        builder.addAnnotation(annotation);
        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addAnnotation(Class<?> annotation) {
        builder.addAnnotation(annotation);
        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addSuperinterface(TypeName superinterface) {
        builder.addSuperinterface(superinterface);
        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addSuperinterface(Type superinterface) {
        builder.addSuperinterface(superinterface);
        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addField(FieldSpec fieldSpec) {
        boolean wasConflict = false;
        for (AutoDataField field : autoDataClass.getFields()) {
            if (field.getName().equals(fieldSpec.name)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Plugin " + currentPluginName + " attempted to add field " + field.getName() + " that conflicts with a model field of the same name.", field.getGetterElement());
                wasConflict = true;
                break;
            }
        }

        if (!wasConflict) {
            builder.addField(fieldSpec);
        }

        return this;
    }

    @Override
    public AutoDataClassBuilderImpl addField(TypeName type, String name, Modifier... modifiers) {
        return addField(FieldSpec.builder(type, name, modifiers).build());
    }

    @Override
    public AutoDataClassBuilderImpl addField(Type type, String name, Modifier... modifiers) {
        return addField(TypeName.get(type), name, modifiers);
    }

    @Override
    public AutoDataClassBuilderImpl addMethod(MethodSpec methodSpec) {
        for (ExecutableElement element : declaredMethods) {
            if (specMatches(methodSpec, element)) {
                return this;
            }
        }

        // Delay adding methods from plugins, this allows the getters to come first in the generated class.
        addedMethods.add(methodSpec);

        return this;
    }

    @Override
    public AutoDataClassBuilder addType(TypeSpec typeSpec) {
        builder.addType(typeSpec);
        return this;
    }

    TypeSpec build() {
        for (MethodSpec spec : addedMethods) {
            builder.addMethod(spec);
        }
        addedMethods.clear();
        return builder.build();
    }

    /**
     * Determine if a declared method matches one that has been added. This allows underriding by
     * silently dropping methods which have already been declared.
     */
    private boolean specMatches(MethodSpec spec, ExecutableElement element) {
        if (!spec.name.equals(element.getSimpleName().toString())) {
            return false;
        }
        if (spec.parameters.size() != element.getParameters().size()) {
            return false;
        }

        List<String> elementTypeNames = Lists.newArrayListWithCapacity(element.getParameters().size());
        List<String> specTypeNames = Lists.newArrayListWithCapacity(spec.parameters.size());

        for (VariableElement param : element.getParameters()) {
            // Don't worry about generics, they are ignored for method resolution.
            elementTypeNames.add(typeUtils.erasure(param.asType()).toString());
        }

        for (ParameterSpec param : spec.parameters) {
            specTypeNames.add(param.type.toString());
        }

        for (int i = 0; i < elementTypeNames.size(); i++) {
            // Use startsWith because spec type may have generic junck on it.
            if (!specTypeNames.get(i).startsWith(elementTypeNames.get(i))) {
                return false;
            }
        }

        return true;
    }
}
