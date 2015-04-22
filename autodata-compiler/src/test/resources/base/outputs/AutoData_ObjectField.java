final class AutoData_ObjectField extends ObjectField {
    private final String test;

    AutoData_ObjectField(String test) {
        if (test == null) {
            throw new NullPointerException("Null test");
        }
        this.test = test;
    }

    @Override
    public String test() {
        return test;
    }
}