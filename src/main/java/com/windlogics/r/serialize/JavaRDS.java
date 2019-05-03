package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Create R-style RDS data files from Java
 *
 * See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the data format definition.
 *
 * An example of usage:
 *
 * <pre>
 * RList data = RNamedList(
 *   "A3", RDataframe(
 *           "size", new RFloat(Arrays.asList(45252d, 45907d)),
 *           "isdir", new RBoolean(Arrays.asList(false, false)),
 *           "mtime", RPOSIXct(Arrays.asList(1360227629.28697d, 1364324320d)),
 *           "ctime", RPOSIXct(Arrays.asList(1543489579.69805d, 1543489579.69805d)),
 *           "atime", RPOSIXct(Arrays.asList(1543450562.88206d, 1543450563.03406d)),
 *           "uid", new RInteger(Arrays.asList(1001, 1010)),
 *           "gid", new RInteger(Arrays.asList(1001, 1001)),
 *           "uname", new RString(Arrays.asList("hornik", "ligges")),
 *           "grname", new RString(Arrays.asList("cranadmin", "cranadmin"))
 *   ).setAttr("row.names", new RString(Arrays.asList("A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"))),
 *   "aaMI", RDataframe(
 *           "size", new RFloat(Arrays.asList(2968d, 3487d)),
 *           "isdir", new RBoolean(Arrays.asList(false, false)),
 *           "mtime", RPOSIXct(Arrays.asList(1119628517d, 1129577058d)),
 *           "ctime", RPOSIXct(Arrays.asList(1543489579.60206, 1543489579.60206)),
 *           "atime", RPOSIXct(Arrays.asList(1543451154.65134, 1543451154.65534)),
 *           "uid", new RInteger(Arrays.asList(0, 0)),
 *           "gid", new RInteger(Arrays.asList(1001, 1001)),
 *           "uname", new RString(Arrays.asList("root", "root")),
 *           "grname", new RString(Arrays.asList("cranadmin", "cranadmin"))
 *   ).setAttr("row.names", new RString(Arrays.asList("aaMI/aaMI_1.0-0.tar.gz", "aaMI/aaMI_1.0-1.tar.gz")))
 * );
 * JavaRDS.writeRDS(data, "myData.rds");
 * </pre>
 *
 *
 * This can then be read into R using `readRDS("myData.rds")`, and it should be identical to the following R structure:
 *
 * <pre>
 * list(
 *   A3 = data.frame(
 *     size = c(45252, 45907),
 *     isdir = c(FALSE, FALSE),
 *     mtime = as.POSIXct(c(1360227629.28697, 1364324320), origin="1970-01-01"),
 *     ctime = as.POSIXct(c(1543489579.69805, 1543489579.69805), origin="1970-01-01"),
 *     atime = as.POSIXct(c(1543450562.88206, 1543450563.03406), origin="1970-01-01"),
 *     uid = c(1001L, 1010L),
 *     gid = c(1001L, 1001L),
 *     uname = c("hornik", "ligges"),
 *     grname = c("cranadmin", "cranadmin"),
 *     row.names = c("A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz")),
 *   aaMI = data.frame(
 *     size = c(2968, 3487),
 *     isdir = c(FALSE, FALSE),
 *     mtime = as.POSIXct(c(1119628517, 1129577058), origin="1970-01-01"),
 *     ctime = as.POSIXct(c(1543489579.60206, 1543489579.60206), origin="1970-01-01"),
 *     atime = as.POSIXct(c(1543451154.65134, 1543451154.65534), origin="1970-01-01"),
 *     uid = c(0L, 0L),
 *     gid = c(1001L, 1001L),
 *     uname = c("root", "root"),
 *     grname = c("cranadmin", "cranadmin"),
 *     row.names = c("aaMI/aaMI_1.0-0.tar.gz", "aaMI/aaMI_1.0-1.tar.gz"))
 * )
 * </pre>
 */
public class JavaRDS {

    private static int version(int v, int p, int s) {
        return (v << 16) | (p << 8) | s;
    }

    public static void writeRDS(RThing x, OutputStream os, boolean compress) throws IOException {
        if (compress) {
            os = new GZIPOutputStream(os);
        }

        DataOutputStream dos = new DataOutputStream(os);

        dos.write(new byte[] { 'X', '\n' });  // Magic "number"
        dos.writeInt(2);                  // Version of RDS spec
        dos.writeInt(version(3, 5, 1));   // Version of R that wrote the file
        dos.writeInt(version(2, 3, 0));   // Minimal version of R needed to read the format

        x.serialize(dos);
    }

    public static void writeRDS(RThing x, OutputStream os) throws IOException {
        writeRDS(x, os, true);
    }

    public static void writeRDS(RThing x, String file) throws IOException {
        writeRDS(x, new FileOutputStream(file));
    }

    ////////////////////////////////////////////////////////////////////////////

    private static int NA_INT = 1 << 31;

    // Shortcuts
    public static RThing RPOSIXct(List<Double> dates) {
        RFloat x = new RFloat(dates);
        // Mutable version of Arrays.asList(...);
        x.setClass("POSIXct", "POSIXt");
        return x;
    }

    public static RList RNamedList(Object... elements) {
        List<String> keys = new ArrayList<>();
        List<RThing> vals = new ArrayList<>();
        for (int i = 0; i < elements.length; i += 2) {
            keys.add((String) elements[i]);
            vals.add((RThing) elements[i + 1]);
        }
        return new RList(vals, new RPairlist().setAttr("names", new RString(keys)));
    }

    public static RThing RDataframe(Object... elements) {
        RList df = RNamedList(elements);
        df.setClass("data.frame");
        int num_rows = df.data.get(0).data.size();
        df.setAttr("row.names", new RInteger(new Integer[]{NA_INT, -num_rows}));
        return df;
    }
}
