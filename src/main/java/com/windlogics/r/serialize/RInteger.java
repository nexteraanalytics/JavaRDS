package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RInteger extends RThing<Integer> {
    public RInteger(List<Integer> d) { this(d, null); }
    public RInteger(Integer[] d) { this(Arrays.asList(d), null); }
    public RInteger(List<Integer> d, RPairlist attrs) { super(d, attrs); }

    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (Integer i : data)
            os.writeInt(i);
    }

    @Override public int type() {
        return 0x0d;
    }
}
