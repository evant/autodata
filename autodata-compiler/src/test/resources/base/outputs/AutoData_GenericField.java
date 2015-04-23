final class AutoData_GenericField<T> extends GenericField<T> {
    private final T test;

    AutoData_GenericField(T test) {
        if (test == null) {
            throw new NullPointerException("Null test");
        }
        this.test = test;
    }

    @Override
    public T getTest() {
        return test;
    }
}