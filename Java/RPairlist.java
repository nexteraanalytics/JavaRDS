import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RPairlist extends RThing<Object> {
    // This class is used for attributes too
    public RPairlist() { this(new ArrayList<>()); }
    public RPairlist(List<Object> d) { this(d, null); }
    public RPairlist(List<Object> d, RPairlist attrs) { super(d, attrs); }

    @Override public void putData(DataOutputStream os) throws IOException {
        for (int i=0; i<data.size(); i+=2) {
            os.writeInt(0x402);
            os.writeInt(1);
            os.writeInt(RString.getStringStart());
            String key = (String) data.get(i);  // Name
            os.writeInt(key.length());
            os.write(key.getBytes());
            ((RThing) data.get(i+1)).serialize(os);        // Value
        }
        new RNull().serialize(os);
    }

    public int length() {
        return data.size() / 2;
    }

    public boolean hasKey(String key) {

        for (int i = 0; i < data.size(); i += 2) {
            if (data.get(i).equals(key))
                return true;
        }
        return false;
    }

    @Override public RPairlist setAttr(String key, RThing value) {
        for (int i = 0; i < data.size(); i += 2) {
            if (data.get(i).equals(key)) {
                data.set(i+1, value);
                return this;
            }
        }
        data.add(key);
        data.add(value);
        return this;
    }
}
