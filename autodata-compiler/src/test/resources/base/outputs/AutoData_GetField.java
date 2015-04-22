final class AutoData_GetField extends GetField {
    private final int test;

    AutoData_GetField(int test) {
        this.test = test;
    }

    @Override
    public int getTest() {
        return test;
    }
}