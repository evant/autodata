package me.tatarka.autodata.compiler;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import me.tatarka.autodata.compiler.internal.AutoDataAnnotationProcessor;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by evan on 4/20/15.
 */
@RunWith(JUnit4.class)
public class BaseTest {
    @Test
    public void emptyClass() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {}"));
    }

    @Test
    public void singlePrimitiveField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract int test();\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {\n" +
                        "    private final int test;\n" +
                        "    \n" +
                        "    AutoData_Test(int test) {\n" +
                        "        this.test = test;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public int test() {\n" +
                        "        return test;\n" +
                        "    }\n" +
                        "}"));
    }

    @Test
    public void singleObjectField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract String test();\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {\n" +
                        "    private final String test;\n" +
                        "    \n" +
                        "    AutoData_Test(String test) {\n" +
                        "        if (test == null) {\n" +
                        "            throw new NullPointerException(\"Null test\");\n" +
                        "        }\n" +
                        "        this.test = test;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public String test() {\n" +
                        "        return test;\n" +
                        "    }\n" +
                        "}"));
    }

    @Test
    public void getMethodField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract int getTest();\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {\n" +
                        "    private final int test;\n" +
                        "    \n" +
                        "    AutoData_Test(int test) {\n" +
                        "        this.test = test;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public int getTest() {\n" +
                        "        return test;\n" +
                        "    }\n" +
                        "}"));
    }

    @Test
    public void getterWithoutReturnErrors() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract void test();\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Abstract method test in class test.Test must have a non-void return type.");
    }

    @Test
    public void getterWithArgumentErrors() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract int test(int arg);\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Abstract method test in class test.Test must not take any arguments.");
    }

    @Test
    public void multipleGettersWithSameNameErrors() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "public abstract class Test {\n" +
                        "    public abstract int test();\n" +
                        "    public abstract int getTest();\n" +
                        "}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("More than one AutoData field called test (getTest and test).");
    }
}
