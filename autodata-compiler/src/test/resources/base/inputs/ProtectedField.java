import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class ProtectedField {
    abstract int getTest();
}