package me.tatarka.autodata.compiler;

import com.google.testing.compile.JavaFileObjects;
import me.tatarka.autodata.compiler.internal.AutoDataAnnotationProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by evan on 4/20/15.
 */
@RunWith(JUnit4.class)
public class ToStringTest {
    @Test
    public void empty() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("tostring/inputs/Empty.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("tostring/outputs/AutoData_Empty.java"));
    }

    @Test
    public void field() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource("tostring/inputs/Field.java"))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("tostring/outputs/AutoData_Field.java"));
    }
}
