package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class JavaRDS {

    // See https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats for the format definition

    private static int version(int v, int p, int s) {
        return (v << 16) | (p << 8) | s;
    }

    public static void write_rds(RThing x, OutputStream os, boolean compress) throws IOException {
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


    public static void write_rds(RThing x, OutputStream os) throws IOException {
        write_rds(x, os, true);
    }

    public static void write_rds(RThing x, String file) throws IOException {
        write_rds(x, new FileOutputStream(file));
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
        int num_rows = df.data.get(0).data.size();
        df.setAttr("row.names", new RInteger(new Integer[]{NA_INT, -num_rows}));
        return df;
    }
}
