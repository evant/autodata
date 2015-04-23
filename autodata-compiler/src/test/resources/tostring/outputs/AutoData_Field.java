final class AutoData_Field extends Field {
    private final int test;

    AutoData_Field(int test) {
        this.test = test;
    }

    @Override
    public String toString() {
        return "Field{" + "test=" + test + "}";
    }

    @Override
    public int test() {
        return test;
    }
}