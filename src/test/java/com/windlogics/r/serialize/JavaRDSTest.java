package com.windlogics.r.serialize;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;


import static com.windlogics.r.serialize.JavaRDS.RNamedList;
import static com.windlogics.r.serialize.JavaRDS.RDataframe;
import static com.windlogics.r.serialize.JavaRDS.RPOSIXct;

public class JavaRDSTest {

    private byte[] toBytes(RThing rt) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaRDS.writeRDS(rt, baos, false);
        return baos.toByteArray();
    }

    @Test
    public void writeRDS() throws IOException {

        RList data = RNamedList(
                "A3", RDataframe(
                        "size", new RFloat(Arrays.asList(45252d, 45907d)),
                        "isdir", new RBoolean(Arrays.asList(false, false)),
                        "mode", new RInteger(Arrays.asList(436, 436)).setClass("octmode"),
                        "mtime", RPOSIXct(Arrays.asList(1360227629.28697d, 1364324320d)),
                        "ctime", RPOSIXct(Arrays.asList(1543489579.69805d, 1543489579.69805d)),
                        "atime", RPOSIXct(Arrays.asList(1543450562.88206d, 1543450563.03406d)),
                        "uid", new RInteger(Arrays.asList(1001, 1010)),
                        "gid", new RInteger(Arrays.asList(1001, 1001)),
                        "uname", new RString(Arrays.asList("hornik", "ligges")),
                        "grname", new RString(Arrays.asList("cranadmin", "cranadmin"))
                ).setAttr("row.names", new RString(Arrays.asList("A3/A3_0.9.1.tar.gz", "A3/A3_0.9.2.tar.gz"))),

                "aaMI", RDataframe(
                        "size", new RFloat(Arrays.asList(2968d, 3487d)),
                        "isdir", new RBoolean(Arrays.asList(false, false)),
                        "mode", new RInteger(Arrays.asList(436, 436)).setClass("octmode"),
                        "mtime", RPOSIXct(Arrays.asList(1119628517d, 1129577058d)),
                        "ctime", RPOSIXct(Arrays.asList(1543489579.60206, 1543489579.60206)),
                        "atime", RPOSIXct(Arrays.asList(1543451154.65134, 1543451154.65534)),
                        "uid", new RInteger(Arrays.asList(0, 0)),
                        "gid", new RInteger(Arrays.asList(1001, 1001)),
                        "uname", new RString(Arrays.asList("root", "root")),
                        "grname", new RString(Arrays.asList("cranadmin", "cranadmin"))
                ).setAttr("row.names", new RString(Arrays.asList("aaMI/aaMI_1.0-0.tar.gz", "aaMI/aaMI_1.0-1.tar.gz")))
        );

        // TODO this file is not byte-for-byte what R's saveRDS would produce, but it deserializes to something that passes R's all.equal() test.
        assertBytes(data, "archive-nongzip.rds");
    }

    @Test
    public void test_integers() throws IOException {
        RThing<Integer> data = new RInteger(Arrays.asList(1, 2, 3, 4, 5));

        assertBytes(data, "1-5.rds");
    }

    @Test
    public void test_strings() throws IOException {
        RThing data = new RString(Arrays.asList("abc", "defg"));

        assertBytes(data, "abc.rds");
    }

    @Test
    public void test_booleans() throws IOException {
        RThing data = new RBoolean(Arrays.asList(true, false, true));
        assertBytes(data, "bool.rds");
    }

    @Test
    public void test_list() throws IOException {
        RList data = RNamedList(
                "A", new RInteger(Arrays.asList(1, 2, 3)),
                "AB", new RInteger(Arrays.asList(1, 2))
        );

        assertBytes(data, "list.rds");
    }

    @Test
    public void test_attributes() throws IOException {
        RThing data = new RInteger(Collections.singletonList(1))
                .setClass("foo")
                .setAttr("bar", new RInteger(Arrays.asList(2, 3)));

        assertBytes(data, "attrs.rds");
    }

    @Test
    public void test_class() throws IOException {
        RThing data = new RInteger(Arrays.asList(436, 436)).setClass("octmode");
        assertBytes(data, "octmode.rds");
    }

    @Test
    public void test_date() throws IOException {
        RThing data = RPOSIXct(Arrays.asList(1360227629.28697013855d, 1364324320d));
        assertBytes(data, "dates.rds");
    }

    @Test
    public void test_dataframes() throws IOException {
        RThing data = RDataframe(
                "x", new RInteger(Arrays.asList(4, 5))
        );
        assertBytes(data, "df.rds");

        data = RDataframe(
                "y", new RInteger(Collections.singletonList(436)).setClass("octmode")
        );

        // TODO this file is not byte-for-byte what R's saveRDS would produce, but it deserializes to something that passes R's all.equal() test.
        assertBytes(data, "ar-1.rds");
    }

    private void assertBytes(RThing data, String s) throws IOException {
        Assert.assertArrayEquals(
                Files.readAllBytes(Paths.get("src/test/resources/" + s)),
                toBytes(data));
    }

}
