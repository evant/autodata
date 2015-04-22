package me.tatarka.autodata.compiler;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import me.tatarka.autodata.compiler.internal.AutoDataAnnotationProcessor;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by evan on 4/21/15.
 */
@RunWith(JUnit4.class)
public class CustomAnnotationTest {
    @Test
    public void emptyClass() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.TestAutoData;\n" +
                        "\n" +
                        "@TestAutoData\n" +
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
                        "\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int h = 1;\n" +
                        "        return h;\n" +
                        "    }\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return \"Test{\" + \"}\";\n" +
                        "    }\n" +
                        "}"));
    }

}
