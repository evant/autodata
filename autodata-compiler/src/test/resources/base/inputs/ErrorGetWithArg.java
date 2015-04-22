import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class ErrorGetWithArg {
    public abstract int test(int arg);
}