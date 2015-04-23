final class AutoData_BooleanField extends BooleanField {
    private final boolean test;

    AutoData_BooleanField(boolean test) {
        this.test = test;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BooleanField) {
            BooleanField that = (BooleanField) o;
            return this.test == that.test();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= test ? 1231 : 1237;
        return h;
    }

    @Override
    public boolean test() {
        return test;
    }
}