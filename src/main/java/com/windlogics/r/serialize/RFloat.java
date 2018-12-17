package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RFloat extends RThing<Double> {
    public RFloat(List<Double> d) { this(d, null); }
    public RFloat(List<Double> d, RPairlist attrs) { super(d, attrs); }

    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (Double i : data)
            os.writeDouble(i);
    }

    @Override public int type() {
        return 0xe;
    }
}
