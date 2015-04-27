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

    static final class Builder extends ArrayField.Builder {
        private int[] test;

        Builder() {
        }

        Builder(ArrayField source) {
            test(source.test());
        }

        @Override
        public ArrayField.Builder test(int[] test) {
            this.test = test.clone();
            return this;
        }

        @Override
        public ArrayField build() {
            String missing = "";
            if (this.test == null) {
                missing += " test";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            ArrayField result = new AutoData_ArrayField(this.test);
            return result;
        }
    }
}
