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
public class EqualsTest {
    @Test
    public void emptyClass() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "import me.tatarka.autodata.plugins.AutoEquals;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "@AutoEquals\n" +
                        "public abstract class Test {}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {\n" +
                        "    @Override\n" +
                        "    public boolean equals(Object o) {\n" +
                        "        if (o == this) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        if (o instanceof Test) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        return false;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int h = 1;\n" +
                        "        return h;\n" +
                        "    }\n" +
                        "}"));
    }

    @Test
    public void singlePrimitiveField() {
            ASSERT.about(javaSource())
                    .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                            "\n" +
                            "import me.tatarka.autodata.base.AutoData;\n" +
                            "import me.tatarka.autodata.plugins.AutoEquals;\n" +
                            "\n" +
                            "@AutoData(defaults = false)\n" +
                            "@AutoEquals\n" +
                            "public abstract class Test {\n" +
                            "    public abstract boolean test();\n" +
                            "}"))
                    .processedWith(new AutoDataAnnotationProcessor())
                    .compilesWithoutError()
                    .and()
                    .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                            "\n" +
                            "final class AutoData_Test extends Test {\n" +
                            "    private final boolean test;\n" +
                            "    \n" +
                            "    AutoData_Test(boolean test) {\n" +
                            "        this.test = test;\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public boolean equals(Object o) {\n" +
                            "        if (o == this) {\n" +
                            "            return true;\n" +
                            "        }\n" +
                            "        if (o instanceof Test) {\n" +
                            "            Test that = (Test) o;\n" +
                            "            return this.test == that.test();\n" +
                            "        }\n" +
                            "        return false;\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public int hashCode() {\n" +
                            "        int h = 1;\n" +
                            "        h *= 1000003;\n" +
                            "        h ^= test ? 1231 : 1237;\n" +
                            "        return h;\n" +
                            "    }\n" +
                            "    \n" +
                            "    @Override\n" +
                            "    public boolean test() {\n" +
                            "        return test;\n" +
                            "    }\n" +
                            "}"));
    }
}
