import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class ErrorMultipleGets {
    public abstract boolean isTest();

    public abstract int getTest();
}
