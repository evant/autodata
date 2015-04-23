package me.tatarka.autodata.compiler.model;

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class provides data about the AutoData class you are generating from. It also allows limited
 * control of what gets generated. For example, you can add additional annotations to the generated
 * methods and fields.
 */
public final class AutoDataClass {
    private TypeElement element;
    private Set<AutoDataField> fields;

    public AutoDataClass(TypeElement element, @Nonnull Collection<AutoDataField> fields) {
        this.element = checkNotNull(element);
        this.fields = new LinkedHashSet<>(checkNotNull(fields));
    }

    public TypeElement getElement() {
        return element;
    }

    public Set<AutoDataField> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    public String getPackageName() {
        return ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
    }

    public String getGenSimpleClassName() {
        return "AutoData_" + element.getSimpleName();
    }

    public String getGenQualifiedClassName() {
        if (Strings.isNullOrEmpty(getPackageName())) {
            return getGenSimpleClassName();
        } else {
            return getPackageName() + "." + getGenSimpleClassName();
        }
    }
}
