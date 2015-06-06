package me.tatarka.autodata.sample;

final class AutoData_Person extends Person {
  private final String name;

  private final int age;

  AutoData_Person(String name, int age) {
    if (name == null) {
      throw new NullPointerException("Null name");}
    this.name = name;
    this.age = age;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public int age() {
    return age;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Person) {
      Person that = (Person) o;
      return this.name.equals(that.name()) && this.age == that.age();}
    return false;
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= name.hashCode();
    h *= 1000003;
    h ^= age;
    return h;
  }

  @Override
  public String toString() {
    return "Person{" + "name=" + name + ", " + "age=" + age + "}";}

  static final class Builder implements Person.Builder {
    private String name;

    private Integer age;

    Builder() {
    }

    Builder(Person source) {
      name(source.name());
      age(source.age());
    }

    @Override
    public Person.Builder name(String name) {
      this.name = name;
      return this;
    }

    @Override
    public Person.Builder age(int age) {
      this.age = age;
      return this;
    }

    @Override
    public Person build() {
      String missing = "";
      if (this.name == null) {
        missing += " name";
      }
      if (this.age == null) {
        missing += " age";
      }
      if(!missing.isEmpty()) {
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      Person result = new AutoData_Person(this.name, this.age);return result;
    }
  }
}
