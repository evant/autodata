package me.tatarka.autodata.util;

import com.google.testing.compile.JavaFileObjects;
import me.tatarka.autodata.compiler.internal.AutoDataAnnotationProcessor;
import org.junit.Test;
import org.junit.runners.Parameterized;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by evan on 5/5/15.
 */
public abstract class AutoDataParameterizedTest {
    @Parameterized.Parameter(0)
    public String source;
    @Parameterized.Parameter(1)
    public String target;

    @Test
    public void test() {
        ASSERT.about(javaSource())
                .that(JavaFileObjects.forResource(source))
                .processedWith(new AutoDataAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource(target));
    }

}
