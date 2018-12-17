package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RString extends RThing<String> {
    private static int STRING_START = 0x40009; // Probably an encoding marker?

    public RString(List<String> d) { this(d, null); }
    public RString(List<String> d, RPairlist attrs) { super(d, attrs); type = 0x10; }
    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (String i : data) {
            os.writeInt(STRING_START);
            os.writeInt(i.length());
            os.write(i.getBytes());
        }
    }

    public static int getStringStart() {
        return STRING_START;
    }
}
