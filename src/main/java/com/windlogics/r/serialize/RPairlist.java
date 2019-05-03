package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An R pairlist, which is also used for attributes on other R objects
 */
public class RPairlist extends RThing<Object> {
    // Use our own member variable instead of `data`, since we have incompatible types
    protected Map<String, RThing> content;

    /**
     * Construct a new pairlist with no entries
     */
    public RPairlist() { this(null); }

    /**
     * Construct a new pairlist with the entries given in `d`.  `d` is stored by reference, not cloned.
     */
    public RPairlist(Map<String, RThing> d) { this(d, null); }

    /**
     * Construct a new pairlist with the entries given in `d` and attributes given in `attrs`.  The
     * arguments are stored by reference, not cloned.
     */
    public RPairlist(Map<String, RThing> d, RPairlist attrs) {
        super(null, attrs);
        content = d == null ? new LinkedHashMap<>() : d;
    }

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

    @Override
    public int type() {
        return 0;
    }

    public int length() {
        return content.size();
    }

    public boolean containsKey(String key) {
        return content.containsKey(key);
    }

    public RPairlist put(String key, RThing value) {
        content.put(key, value);
        return this;
    }
}
