import me.tatarka.autodata.base.AutoData;
import me.tatarka.autodata.plugins.AutoBuilder;

@AutoData(defaults = false)
@AutoBuilder
public abstract class ArrayField {
    public abstract int[] test();

    @AutoData.Builder
    public static abstract class Builder {
        public abstract Builder test(int[] test);

        public abstract ArrayField build();
    }
}
