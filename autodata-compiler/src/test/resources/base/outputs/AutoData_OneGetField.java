final class AutoData_OneGetField extends OneGetField {
    private final int getTest;
    private final int notGetTest;

    AutoData_GetField(int getTest, int notGetTest) {
        this.getTest = getTest;
        this.notGetTest = notGetTest;
    }

    @Override
    public int getTest() {
        return getTest;
    }

    @Override
    public int notGetTest() {
        return notGetTest;
    }
}