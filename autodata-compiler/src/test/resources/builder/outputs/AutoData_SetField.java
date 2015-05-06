final class AutoData_SetField extends SetField {
    private final int test;

    AutoData_Field(int test) {
        this.test = test;
    }

    @Override
    public int getTest() {
        return test;
    }

    static final class Builder extends SetField.Builder {
        private Integer test;

        Builder() {
        }

        Builder(SetField source) {
            setTest(source.getTest());
        }

        @Override
        public SetField.Builder setTest(int test) {
            this.test = test;
            return this;
        }

        @Override
        public SetField build() {
            String missing = "";
            if (this.test == null) {
                missing += " test";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            SetField result = new AutoData_SetField(this.test);
            return result;
        }
    }
}
