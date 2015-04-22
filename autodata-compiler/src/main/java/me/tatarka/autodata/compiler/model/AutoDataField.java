package me.tatarka.autodata.compiler.model;

import com.google.common.collect.Sets;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about a field that will be generated based on an AutoData getter.
 */
public final class AutoDataField {
    private String name;
    private TypeName type;
    private boolean nullable;
    private Set<AnnotationSpec> annotations = Sets.newLinkedHashSet();

    public AutoDataField(@Nonnull String name, @Nonnull TypeName type, boolean nullable) {
        this.name = checkNotNull(name);
        this.type = checkNotNull(type);
        this.nullable = nullable;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable && !type.isPrimitive();
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

        AutoDataField that = (AutoDataField) o;

        if (!name.equals(that.name)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
