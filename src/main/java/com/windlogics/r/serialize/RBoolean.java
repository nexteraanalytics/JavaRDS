package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * An R logical vector
 */
public class RBoolean extends RThing<Boolean> {
    public RBoolean(List<Boolean> d) { this(d, null); }
    public RBoolean(List<Boolean> d, RPairlist attrs) { super(d, attrs); }

    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (Boolean i : data)
            os.writeInt(i ? 1 : 0);
    }

    @Override public int type() {
        return 0xa;
    }
}
