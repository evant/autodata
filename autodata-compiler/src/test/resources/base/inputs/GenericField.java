import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class GenericField<T> {
    public abstract T getTest();
}