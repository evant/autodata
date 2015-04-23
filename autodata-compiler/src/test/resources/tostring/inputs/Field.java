import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoToString;

@AutoData(defaults = false)
@AutoToString
public abstract class Field {
    public abstract int test();
}