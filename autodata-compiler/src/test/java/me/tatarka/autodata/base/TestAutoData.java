package me.tatarka.autodata.base;

import me.tatarka.autodata.plugins.AutoEquals;
import me.tatarka.autodata.plugins.AutoToString;

/**
 * Created by evan on 4/21/15.
 */
@AutoData(defaults = false)
@AutoEquals
@AutoToString
public @interface TestAutoData {
}
