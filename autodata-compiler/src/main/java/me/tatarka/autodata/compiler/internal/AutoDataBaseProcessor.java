package me.tatarka.autodata.compiler.internal;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.compiler.AutoDataProcessor;
import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;
import me.tatarka.autodata.compiler.model.AutoDataField;
import me.tatarka.autodata.compiler.model.AutoDataGetterMethod;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;

/**
 * Created by evan on 4/20/15.
 */
// This one isn't a service since it's hard-coded to always run.
public class AutoDataBaseProcessor implements AutoDataProcessor<AutoData> {
    @Override
    public Class<AutoData> forAnnotation() {
        return AutoData.class;
    }

    @Override
    public void init(ProcessingEnvironment env) {

    }

    @Override
    public void process(AutoData autoData, AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilderInterface) {
        // We shall cast here to get some extra secret-sauce not available to other plugins.
        AutoDataClassBuilderImpl genClassBuilder = (AutoDataClassBuilderImpl) genClassBuilderInterface;

        genClassBuilder.builder.addModifiers(Modifier.FINAL)
                .superclass(TypeName.get(autoDataClass.getElement().asType()));


        for (TypeParameterElement typeElement : autoDataClass.getElement().getTypeParameters()) {
            List<? extends TypeMirror> bounds = typeElement.getBounds();
            TypeName[] boundNames = new TypeName[typeElement.getBounds().size()];
            for (int i = 0; i < boundNames.length; i++) {
                boundNames[i] = TypeName.get(bounds.get(i));
            }
            genClassBuilder.builder.addTypeVariable(TypeVariableName.get(typeElement.getSimpleName().toString(), boundNames));
        }

        Collection<AutoDataField> fields = autoDataClass.getFields();
        if (fields.isEmpty()) {
            return;
        }

        // Add constructor that takes all the fields as arguments.
        {
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
            for (AutoDataField field : fields) {
                TypeMirror type = field.getType();
                constructor.addParameter(TypeName.get(type), field.getName());

                boolean checkNull = !type.getKind().isPrimitive() && !field.isNullable();
                if (checkNull) {
                    constructor.beginControlFlow("if ($L == null)", field.getName())
                            .addCode("throw new $T(\"Null $L\");", NullPointerException.class, field.getName())
                            .endControlFlow();
                }
                constructor.addStatement("this.$L = $L", field.getName(), field.getName());
            }
            genClassBuilder.builder.addMethod(constructor.build());
        }

        // Private fields
        for (AutoDataField field : autoDataClass.getFields()) {
            FieldSpec.Builder builder = FieldSpec.builder(TypeName.get(field.getType()), field.getName(), Modifier.PRIVATE, Modifier.FINAL);
//            for (AnnotationMirror annotationMirror : field.getGetterElement().getAnnotationMirrors()) {
//                AnnotationSpec spec = AnnotationSpec.builder(
//                        ClassName.get((TypeElement) annotationMirror.getAnnotationType().asElement())).build();
//                builder.addAnnotation(spec);
//            }
            genClassBuilder.builder.addField(builder.build());
        }

        // Getter methods
        for (AutoDataField field : autoDataClass.getFields()) {
            AutoDataGetterMethod method = field.getGetterMethod();

            MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getName())
                    .addAnnotation(Override.class)
                    .returns(TypeName.get(field.getType()));

            for (Modifier modifier : method.getElement().getModifiers()) {
                if (modifier == Modifier.ABSTRACT) {
                    continue;
                }
                builder.addModifiers(modifier);
            }

//            for (AnnotationSpec annotation : method.getAnnotations()) {
//                builder.addAnnotation(annotation);
//            }
            builder.addStatement("return $L", field.getName());
            genClassBuilder.builder.addMethod(builder.build());
        }

    }
}
