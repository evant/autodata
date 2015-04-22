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
    public void empty() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/Empty.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("base/outputs/AutoData_Empty.java"));
    }

    @Test
    public void primitiveField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/PrimitiveField.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("base/outputs/AutoData_PrimitiveField.java"));
    }

    @Test
    public void objectField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/ObjectField.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("base/outputs/AutoData_ObjectField.java"));
    }

    @Test
    public void getField() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("base/inputs/GetField.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("base/outputs/AutoData_GetField.java"));
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
                .withErrorContaining("More than one AutoData field called test in class ErrorMultipleGets (getTest and test).");
    }
}
