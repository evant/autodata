import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoBuilder;

@AutoData(defaults = false)
@AutoBuilder
public abstract class SetField {
    public abstract int getTest();

    @AutoData.Builder
    public static abstract class Builder {
        public abstract Builder setTest(int test);

        public abstract SetField build();
    }
}
