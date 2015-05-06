package me.tatarka.autodata.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by evan on 5/5/15.
 */
public class AutoDataParams {
    public static AutoDataParams of(String base) {
        return new AutoDataParams(base);
    }

    private String base;

    private AutoDataParams(String base) {
        this.base = base;
    }

    public Collection<String[]> with(String... args) {
        List<String[]> result = new ArrayList<>(args.length);
        for (String arg : args) {
            String source = base + "/inputs/" + arg + ".java";
            String target = base + "/outputs/AutoData_"  + arg + ".java";
            result.add(new String[] { source, target });
        }

        return result;
    }
}
