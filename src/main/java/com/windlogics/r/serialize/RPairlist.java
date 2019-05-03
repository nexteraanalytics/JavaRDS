package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An R pairlist, which is also used for attributes on other R objects
 */
public class RPairlist extends RThing<Object> {
    // This class is used for attributes too
    public RPairlist() { this(new ArrayList<>()); }
    public RPairlist(List<Object> d) { this(d, null); }
    public RPairlist(List<Object> d, RPairlist attrs) {
        super(d, attrs);
        data = null;  // Shouldn't be used
        content = new LinkedHashMap<>();
    }

    protected Map<String, RThing> content;

    @Override public void putData(DataOutputStream os) throws IOException {
        for (Map.Entry<String, RThing> e : content.entrySet()) {
            os.writeInt(0x402);
            os.writeInt(1);
            os.writeInt(RString.getStringStart());
            String key = e.getKey();  // Name
            os.writeInt(key.length());
            os.write(key.getBytes());
            e.getValue().serialize(os);        // Value
        }
        new RNull().serialize(os);
    }

    public int length() {
        return content.size();
    }

    public boolean hasKey(String key) {
        return content.containsKey(key);
    }

    @Override public RPairlist setAttr(String key, RThing value) {
        content.put(key, value);
        return this;
    }
}
