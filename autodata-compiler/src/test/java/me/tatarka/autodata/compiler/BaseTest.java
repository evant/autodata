package me.tatarka.autodata.compiler;

import com.google.testing.compile.JavaFileObjects;
import me.tatarka.autodata.compiler.internal.AutoDataAnnotationProcessor;
import me.tatarka.autodata.util.AutoDataParameterizedTest;
import me.tatarka.autodata.util.AutoDataParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by evan on 4/20/15.
 */
@RunWith(JUnit4.class)
public class BaseTest {
    @RunWith(Parameterized.class)
    public static class BaseParameterized extends AutoDataParameterizedTest {
        @Parameterized.Parameters
        public static Collection<String[]> testData() {
            return AutoDataParams.of("base").with(
                    "Empty",
                    "PrimitiveField",
                    "ObjectField",
                    "ArrayField",
                    "GetField",
                    "OneGetField",
                    "ProtectedField",
                    "GenericField",
                    "ComplexGenerics",
                    "Serialize"
            );
        }
    }

    @Test
    public void nested() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/Nested.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("base/outputs/AutoData_Nested_Inner.java"));
    }

    @Test
    public void getNoReturn() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/ErrorGetNoReturn.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Abstract method test in class ErrorGetNoReturn must have a non-void return type.");
    }

    @Test
    public void getWithArg() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/ErrorGetWithArg.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Abstract method test in class ErrorGetWithArg must not take any arguments.");
    }

    @Test
    public void multipleGets() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/ErrorMultipleGets.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("More than one AutoData field called test in class ErrorMultipleGets (getTest and isTest).");
    }

    @Test
    public void objectArrayField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/ErrorObjectArrayField.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Method test cannot return a non-primitive array in class ErrorObjectArrayField.");
    }
}
