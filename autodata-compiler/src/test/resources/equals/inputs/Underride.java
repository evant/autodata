import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoEquals;

@AutoData(defaults = false)
@AutoEquals
public abstract class Underride {
    @Override
    public boolean equals(Object other) {
        return true;
    }
}
