package me.tatarka.autodata.compiler.model;

import com.google.common.collect.BiMap;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.tatarka.autodata.compiler.util.JavaPoetUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class provides data about the AutoData class you are generating from. It also allows limited
 * control of what gets generated. For example, you can add additional annotations to the generated
 * methods and fields.
 */
public final class AutoDataClass {
    private String packageName;
    private TypeName type;
    private BiMap<AutoDataField, AutoDataGetterMethod> fields;
    @Nullable
    private AutoDataBuilderClass builder;

    public AutoDataClass(@Nonnull String packageName, @Nonnull TypeName type, @Nonnull BiMap<AutoDataField, AutoDataGetterMethod> fields, @Nullable AutoDataBuilderClass builder) {
        this.packageName = checkNotNull(packageName);
        this.type = checkNotNull(type);
        this.fields = checkNotNull(fields);
        this.builder = builder;
    }

    public String getPackageName() {
        return packageName;
    }

    public TypeName getType() {
        return type;
    }

    public Collection<AutoDataField> getFields() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Collection<AutoDataGetterMethod> getGetterMethods() {
        return Collections.unmodifiableCollection(fields.values());
    }

    @Nullable
    public AutoDataBuilderClass getBuilder() {
        return builder;
    }

    public String getGenSimpleClassName() {
        return "AutoData_" + JavaPoetUtil.getSimpleTypeName(type);
    }

    public String getGenQualifiedClassName() {
        if (packageName.isEmpty()) {
            return getGenSimpleClassName();
        } else {
            return packageName + "." + getGenSimpleClassName();
        }
    }

    public AutoDataField getFieldForGetterMethod(AutoDataGetterMethod method) {
        return fields.inverse().get(method);
    }

    public AutoDataGetterMethod getGetterMethodForField(AutoDataField field) {
        return fields.get(field);
    }
}
