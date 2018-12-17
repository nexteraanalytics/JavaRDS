package com.windlogics.r.serialize;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


import static com.windlogics.r.serialize.JavaRDS.NamedList;
import static com.windlogics.r.serialize.JavaRDS.RDataframe;
import static com.windlogics.r.serialize.JavaRDS.RDate;

public class JavaRDSTest {

    @Test
    public void write_rds() throws IOException {


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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaRDS.write_rds(data, baos, false);

        byte[] expected = Files.readAllBytes(Paths.get("src/test/resources/archive-nongzip.rds"));

        Assert.assertArrayEquals(expected, baos.toByteArray());
    }
}