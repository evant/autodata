import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class ErrorMultipleGets {
    public abstract int test();

    public abstract int getTest();
}
