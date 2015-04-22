final class AutoData_PrimitiveField extends PrimitiveField {
    private final int test;

    AutoData_PrimitiveField(int test) {
        this.test = test;
    }

    @Override
    public int test() {
        return test;
    }
}