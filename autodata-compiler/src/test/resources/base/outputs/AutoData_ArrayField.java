final class AutoData_ArrayField extends ArrayField {
    private final int[] test;

    AutoData_ArrayField(int[] test) {
        if (test == null) {
            throw new NullPointerException("Null test");
        }
        this.test = test;
    }

    @Override
    public int[] test() {
        return test.clone();
    }
}