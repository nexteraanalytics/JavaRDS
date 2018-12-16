import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaRDS {

    // See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the format definition

    public static void main(String[] args) throws IOException {

        RList data = NamedList(
                "A3", RDataframe(
                        "size", new RFloat(Arrays.asList(45252d, 45907d)),
                        "isdir", new RBoolean(Arrays.asList(false, false)),
                        "mode", new RInteger(Arrays.asList(436, 436)).setClass("octmode"),
                        "mtime", RDate(Arrays.asList(1360227629.28697d, 1364324320d)),
                        "ctime", RDate(Arrays.asList(1543489579.69805d, 1543489579.69805d)),
                        "atime", RDate(Arrays.asList(1543450562.88206d, 1543450563.03406d)),
                        "uid", new RInteger(Arrays.asList(1001, 1010)),
                        "gid", new RInteger(Arrays.asList(1001, 1001)),
                        "uname", new RString(Arrays.asList("hornik", "ligges")),
                        "grname", new RString(Arrays.asList("cranadmin", "cranadmin"))
                ).setAttr("row.names", new RString(Arrays.asList("A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"))));

        write_rds(data, "dat.rds");
    }

    public static int version(int v, int p, int s) {
        return (v << 16) | (p << 8) | s;
    }

    public static void write_rds(RThing x, String file) throws IOException {

        DataOutputStream os = new DataOutputStream(new FileOutputStream(file));

        os.write(new byte[] { 'X', '\n' });  // Magic "number"
        os.writeInt(2);                  // Version of RDS spec
        os.writeInt(version(3, 5, 1));   // Version of R which wrote the file
        os.writeInt(version(2, 3, 0));   // Minimal version of R needed to read the format

        x.serialize(os);
    }

    ////////////////////////////////////////////////////////////////////////////

    private static int NA_INT = 1 << 31;

    // Shortcuts
    public static RThing RDate(List<Double> dates) {
        RFloat x = new RFloat(dates);
        // Mutable version of Arrays.asList(...);
        x.setClass("POSIXct", "POSIXt");
        return x;
    }

    public static RList NamedList(Object... elements) {
        List<String> keys = new ArrayList<>();
        List<RThing> vals = new ArrayList<>();
        for (int i = 0; i < elements.length; i += 2) {
            keys.add((String) elements[i]);
            vals.add((RThing) elements[i + 1]);
        }
        return new RList(vals, new RPairlist().setAttr("names", new RString(keys)));
    }

    public static RThing RDataframe(Object... elements) {
        RList df = NamedList(elements);
        df.setClass("data.frame");
        // Mutable version of Arrays.asList(...);
        df.setAttr("row.names", new RInteger(new Integer[]{NA_INT, -1}));
        return df;
    }
}
