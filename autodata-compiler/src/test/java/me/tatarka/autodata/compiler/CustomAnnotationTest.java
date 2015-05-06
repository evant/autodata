package me.tatarka.autodata.compiler;

import me.tatarka.autodata.util.AutoDataParameterizedTest;
import me.tatarka.autodata.util.AutoDataParams;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * Created by evan on 4/21/15.
 */
public class CustomAnnotationTest {
    @RunWith(Parameterized.class)
    public static class CustomAnnotationParamterized extends AutoDataParameterizedTest {
        @Parameterized.Parameters
        public static Collection<String[]> testData() {
            return AutoDataParams.of("custom-annotation").with(
                    "Empty"
            );
        }
    }
}
