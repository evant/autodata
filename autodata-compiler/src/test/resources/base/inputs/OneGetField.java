import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class OneGetField {
    public abstract int getTest();
    public abstract int notGetTest();
}