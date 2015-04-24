final class AutoData_Field extends Field {
    private final int test;

    AutoData_Field(int test) {
        this.test = test;
    }

    @Override
    public int test() {
        return test;
    }

    static final class Builder extends Field.Builder {
        private Integer test;

        Builder() {
        }

        Builder(Field source) {
            test(source.test());
        }

        @Override
        public Field.Builder test(int test) {
            this.test = test;
            return this;
        }

        @Override
        public Field build() {
            String missing = "";
            if (this.test == null) {
                missing += " test";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            Field result = new AutoData_Field(this.test);
            return result;
        }
    }
}
