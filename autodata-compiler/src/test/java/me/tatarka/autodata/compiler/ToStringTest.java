package me.tatarka.autodata.compiler;

import me.tatarka.autodata.util.AutoDataParameterizedTest;
import me.tatarka.autodata.util.AutoDataParams;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * Created by evan on 4/20/15.
 */
public class ToStringTest {
    @RunWith(Parameterized.class)
    public static class ToStringParamterized extends AutoDataParameterizedTest {
        @Parameterized.Parameters
        public static Collection<String[]> testData() {
            return AutoDataParams.of("tostring").with(
                    "Empty",
                    "Field"
            );
        }
    }
}
