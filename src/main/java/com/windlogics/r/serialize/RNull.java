package com.windlogics.r.serialize;

import java.io.DataOutputStream;

/**
 * An R NULL value (not NA or NaN or any of those things)
 */
public class RNull extends RThing<Object> {
    public RNull() { super(null); }
    @Override public void putData(DataOutputStream os) {
        // A NULL object only puts its type, not any other data
    }

    @Override public int type() {
        return 0xfe;
    }
}
