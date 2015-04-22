package me.tatarka.autodata.compiler.model;

import com.google.common.collect.BiMap;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about the optional builder that and AutoData class my provide.
 */
public final class AutoDataBuilderClass {
    private TypeName type;
    private BiMap<AutoDataField, AutoDataSetterMethod> fields;
    private AutoDataGetterMethod buildMethod;
    private boolean isInterface;

    public AutoDataBuilderClass(@Nonnull TypeName type, @Nonnull BiMap<AutoDataField, AutoDataSetterMethod> fields, @Nonnull AutoDataGetterMethod buildMethod, boolean isInterface) {
        this.type = checkNotNull(type);
        this.fields = checkNotNull(fields);
        this.buildMethod = checkNotNull(buildMethod);
        this.isInterface = isInterface;
    }

    public TypeName getType() {
        return type;
    }
    
    public Collection<AutoDataSetterMethod> getSetterMethods() {
        return Collections.unmodifiableCollection(fields.values());
    }
    
    public AutoDataGetterMethod getBuildMethod() {
        return buildMethod;
    }
    
    public boolean isInterface() {
        return isInterface;
    }
    
    public AutoDataField getFieldForSetterMethod(AutoDataSetterMethod method) {
        return fields.inverse().get(method);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoDataBuilderClass that = (AutoDataBuilderClass) o;

        if (isInterface != that.isInterface) return false;
        if (!type.equals(that.type)) return false;
        if (!fields.equals(that.fields)) return false;
        return buildMethod.equals(that.buildMethod);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + buildMethod.hashCode();
        result = 31 * result + (isInterface ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AutoDataBuilderClass{" +
                "type=" + type +
                ", fields=" + fields +
                ", buildMethod=" + buildMethod +
                ", isInterface=" + isInterface +
                '}';
    }

}
