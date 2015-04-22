final class AutoData_Empty extends Empty {
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Empty) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        return h;
    }

    @Override
    public String toString() {
        return "Empty{" + "}";
    }
}