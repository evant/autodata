import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoBuilder;

@AutoData(defaults = false)
@AutoBuilder
public abstract class Empty {
    @AutoData.Builder
    public static abstract class Builder {
        public abstract Empty build();
    }
}
