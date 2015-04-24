package me.tatarka.autodata.compiler.model;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;

/**
 * Created by evan on 4/22/15.
 */
public interface AutoDataClassBuilder {
    AutoDataClassBuilder addAnnotation(AnnotationSpec annotationSpec);

    AutoDataClassBuilder addAnnotation(ClassName annotation);

    AutoDataClassBuilder addAnnotation(Class<?> annotation);

    AutoDataClassBuilder addSuperinterface(TypeName superinterface);

    AutoDataClassBuilder addSuperinterface(Type superinterface);

    AutoDataClassBuilder addField(FieldSpec fieldSpec);

    AutoDataClassBuilder addField(TypeName type, String name, Modifier... modifiers);

    AutoDataClassBuilder addField(Type type, String name, Modifier... modifiers);

    AutoDataClassBuilder addMethod(MethodSpec methodSpec);

    AutoDataClassBuilder addType(TypeSpec typeSpec);
}
