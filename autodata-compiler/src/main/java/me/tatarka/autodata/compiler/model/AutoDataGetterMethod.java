package me.tatarka.autodata.compiler.model;

import com.google.common.collect.Sets;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about a getter method that will be generated based on an AutoData getter.
 */
public final class AutoDataGetterMethod {
    private String name;
    private TypeName returnType;
    private Set<AnnotationSpec> annotations = Sets.newLinkedHashSet();

    public AutoDataGetterMethod(@Nonnull String name, @Nonnull TypeName returnType) {
        this.name = checkNotNull(name);
        this.returnType = checkNotNull(returnType);
    }

    public String getName() {
        return name;
    }

    public TypeName getReturnType() {
        return returnType;
    }

    public Set<AnnotationSpec> getAnnotations() {
        return Collections.unmodifiableSet(annotations);
    }

    public void addAnnotation(AnnotationSpec annotation) {
        annotations.add(annotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoDataGetterMethod that = (AutoDataGetterMethod) o;

        if (!name.equals(that.name)) return false;
        return returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + returnType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
