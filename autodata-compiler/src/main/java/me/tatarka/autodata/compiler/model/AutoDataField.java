package me.tatarka.autodata.compiler.model;

import javax.annotation.Nonnull;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about a field that will be generated based on an AutoData getter.
 */
public final class AutoDataField {
    private String name;
    private AutoDataGetterMethod getterMethod;

    private TypeMirror type;
    private boolean isNullable;

    public AutoDataField(@Nonnull String name, @Nonnull AutoDataGetterMethod getterMethod) {
        this.name = checkNotNull(name);
        this.getterMethod = checkNotNull(getterMethod);

        this.type = getterMethod.getElement().getReturnType();
        if (!type.getKind().isPrimitive()) { // Can't be nullable if a primitive type.
            this.isNullable = hasNullableAnnotation(getterMethod.getElement());
        }
    }

    public String getName() {
        return name;
    }

    public TypeMirror getType() {
        return type;
    }

    public AutoDataGetterMethod getGetterMethod() {
        return getterMethod;
    }

    public ExecutableElement getGetterElement() {
        return getterMethod.getElement();
    }

    public boolean isNullable() {
        return isNullable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoDataField that = (AutoDataField) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    private static boolean hasNullableAnnotation(Element element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            Name name = annotationMirror.getAnnotationType().asElement().getSimpleName();
            if (name.contentEquals("Nullable")) {
                return true;
            }
        }
        return false;
    }
}
