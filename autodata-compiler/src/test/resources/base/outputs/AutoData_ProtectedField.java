final class AutoData_ProtectedField extends ProtectedField {
    private final int test;

    AutoData_ProtectedField(int test) {
        this.test = test;
    }

    @Override
    int getTest() {
        return test;
    }
}