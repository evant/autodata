import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoEquals;

@AutoData(defaults = false)
@AutoEquals
public abstract class BooleanField {
    public abstract boolean test();
}
