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
    public void empty() {
            ASSERT.about(javaSource())
                    .that(JavaFileObjects.forResource("equals/inputs/Empty.java"))
                    .processedWith(new AutoDataAnnotationProcessor())
                    .compilesWithoutError()
                    .and()
                    .generatesSources(JavaFileObjects.forResource("equals/outputs/AutoData_Empty.java"));
    }

    @Test
    public void primitiveField() {
            ASSERT.about(javaSource())
                    .that(JavaFileObjects.forResource("equals/inputs/PrimitiveField.java"))
                    .processedWith(new AutoDataAnnotationProcessor())
                    .compilesWithoutError()
                    .and()
                    .generatesSources(JavaFileObjects.forResource("equals/outputs/AutoData_PrimitiveField.java"));
    }
}
