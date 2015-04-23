package me.tatarka.autodata.compiler;

import com.squareup.javapoet.TypeSpec;
import me.tatarka.autodata.compiler.model.AutoDataClass;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.annotation.Annotation;

/**
 * Created by evan on 4/20/15.
 */
public interface AutoDataProcessor<T extends Annotation> {
    Class<T> forAnnotation();

    void init(ProcessingEnvironment env);

    void process(T annotation, AutoDataClass autoDataClass, TypeSpec.Builder genClassBuilder);
}
