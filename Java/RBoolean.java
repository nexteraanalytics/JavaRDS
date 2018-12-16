import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RBoolean extends RThing<Boolean> {
    protected static int type = 0xa;

    public RBoolean(List<Boolean> d) { this(d, null); }
    public RBoolean(List<Boolean> d, RPairlist attrs) { super(d, attrs); type = 0xa; }

    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (Boolean i : data)
            os.writeInt(i ? 1 : 0);
    }
}
