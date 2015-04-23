final class AutoData_ComplexGenerics<A, B extends Comparable<A>> extends ComplexGenerics<A, B> {
    private final A a;
    private final B b;

    AutoData_GenericField(A a, B b) {
        if (a == null) {
            throw new NullPointerException("Null a");
        }
        this.a = a;
        if (b == null) {
            throw new NullPointerException("Null b");
        }
        this.b = b;
    }

    @Override
    public A getA() {
        return a;
    }

    @Override
    public B getB() {
        return b;
    }
}