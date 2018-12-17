package com.windlogics.r.serialize;

import java.io.DataOutputStream;

public class RNull extends RThing<Object> {
    public RNull() { super(null); type = 0xfe; }
    @Override public void putData(DataOutputStream os) { }
}
