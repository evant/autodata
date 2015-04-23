import me.tatarka.autodata.base.AutoData;

@AutoData(defaults = false)
public abstract class ComplexGenerics<A, B extends Comparable<A>> {
    public abstract A getA();
    public abstract B getB();
}