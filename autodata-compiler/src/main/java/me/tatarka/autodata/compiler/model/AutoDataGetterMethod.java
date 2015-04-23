package me.tatarka.autodata.compiler.model;

import javax.annotation.Nonnull;
import javax.lang.model.element.ExecutableElement;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data about a getter method that will be generated based on an AutoData getter.
 */
public final class AutoDataGetterMethod {
    private ExecutableElement element;

    public AutoDataGetterMethod(@Nonnull ExecutableElement element) {
        this.element = checkNotNull(element);

    }

    public String getName() {
        return element.getSimpleName().toString();
    }

    public ExecutableElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoDataGetterMethod that = (AutoDataGetterMethod) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
