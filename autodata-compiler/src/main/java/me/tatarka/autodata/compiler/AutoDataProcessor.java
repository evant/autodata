package me.tatarka.autodata.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;

import me.tatarka.autodata.compiler.model.AutoDataClass;

/**
 * Created by evan on 4/20/15.
 */
public interface AutoDataProcessor<T extends Annotation> {
    Class<T> forAnnotation();

    void process(T annotation, AutoDataClass autoDataClass, TypeSpec.Builder genClassBuilder);
}
