import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class RFloat extends RThing<Double> {
    public RFloat(List<Double> d) { this(d, null); }
    public RFloat(List<Double> d, RPairlist attrs) { super(d, attrs); type = 0xe; }

    @Override public void putData(DataOutputStream os) throws IOException {
        os.writeInt(data.size());
        for (Double i : data)
            os.writeDouble(i);
    }
}
