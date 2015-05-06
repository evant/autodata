import me.tatarka.autodata.base.AutoData;

import java.io.Serializable;

@AutoData(defaults = false)
public abstract class Serialize implements Serializable {
    private static final long serialVersionUID = 1234L;
}
