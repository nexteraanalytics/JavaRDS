package com.windlogics.r.serialize;

import java.io.DataOutputStream;

public class RNull extends RThing<Object> {
    public RNull() { super(null); }
    @Override public void putData(DataOutputStream os) { }

    @Override public int type() {
        return 0xfe;
    }
}
