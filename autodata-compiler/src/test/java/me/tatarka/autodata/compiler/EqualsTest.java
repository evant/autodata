package me.tatarka.autodata.compiler;

import me.tatarka.autodata.util.AutoDataParameterizedTest;
import me.tatarka.autodata.util.AutoDataParams;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * Created by evan on 4/20/15.
 */
public class EqualsTest {
    @RunWith(Parameterized.class)
    public static class EqualsParamterized extends AutoDataParameterizedTest {
        @Parameterized.Parameters
        public static Collection<String[]> testData() {
            return AutoDataParams.of("equals").with(
                    "Empty",
                    "BooleanField",
                    "Underride"
            );
        }
    }
}
