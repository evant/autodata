final class AutoData_Empty extends Empty {
    static final class Builder extends Empty.Builder {
        Builder() {
        }

        Builder(Empty source) {
        }

        @Override
        public Empty build() {
            Empty result = new AutoData_Empty();
            return result;
        }
    }
}
