package me.tatarka.autodata.compiler;

import java.lang.annotation.Annotation;

import javax.annotation.processing.ProcessingEnvironment;

import me.tatarka.autodata.compiler.model.AutoDataClass;
import me.tatarka.autodata.compiler.model.AutoDataClassBuilder;

/**
 * Created by evan on 4/20/15.
 */
public interface AutoDataProcessor<T extends Annotation> {
    void init(ProcessingEnvironment env);

    void process(T annotation, AutoDataClass autoDataClass, AutoDataClassBuilder genClassBuilder);
}
