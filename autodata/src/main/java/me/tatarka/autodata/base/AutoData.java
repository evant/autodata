package me.tatarka.autodata.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface AutoData {
    boolean defaults() default true;

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    @interface Builder {
    }
}
