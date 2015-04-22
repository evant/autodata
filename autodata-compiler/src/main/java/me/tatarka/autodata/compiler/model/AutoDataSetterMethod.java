package me.tatarka.autodata.compiler.model;

import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about a getter method that will be generated based on an AutoData getter.
 */
public final class AutoDataSetterMethod {
    private String name;
    private TypeName argType;

    public AutoDataSetterMethod(@Nonnull String name, @Nonnull TypeName argType) {
        this.name = checkNotNull(name);
        this.argType = checkNotNull(argType);
    }

    public String getName() {
        return name;
    }

    public TypeName getArgType() {
        return argType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoDataSetterMethod that = (AutoDataSetterMethod) o;

        if (!name.equals(that.name)) return false;
        return argType.equals(that.argType);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + argType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
