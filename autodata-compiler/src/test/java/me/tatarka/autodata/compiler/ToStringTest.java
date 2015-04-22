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
public class ToStringTest {
    @Test
    public void emptyClass() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forSourceString("test.Test", "package test;\n" +
                        "\n" +
                        "import me.tatarka.autodata.base.AutoData;\n" +
                        "import me.tatarka.autodata.plugins.AutoToString;\n" +
                        "\n" +
                        "@AutoData(defaults = false)\n" +
                        "@AutoToString\n" +
                        "public abstract class Test {}"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forSourceString("test.AutoData_Test", "package test;\n" +
                        "\n" +
                        "final class AutoData_Test extends Test {\n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return \"Test{\" + \"}\";" +
                        "    }\n" +
                        "}"));
    }
}
