package me.tatarka.autodata.compiler.model;

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
    private String genClassName;
    private TypeElement element;
    private Set<AutoDataField> fields;

    public AutoDataClass(String genClassName, TypeElement element, @Nonnull Collection<AutoDataField> fields) {
        this.genClassName = genClassName;
        this.element = checkNotNull(element);
        this.fields = new LinkedHashSet<>(checkNotNull(fields));
    }

    public String getGenClassName() {
        return genClassName;
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
}
