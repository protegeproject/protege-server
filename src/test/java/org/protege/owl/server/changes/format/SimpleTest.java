package org.protege.owl.server.changes.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {"unit.test" })
@SuppressWarnings("deprecation")
public class SimpleTest {
    
    @Test
    public static void testInt() throws IOException {
        int i = 128 + 256 + 512;
        File tmp = File.createTempFile("SimpleTest", ".ser");
        OutputStream os = new FileOutputStream(tmp);
        IOUtils.writeInt(os, i);
        os.flush();
        os.close();
        InputStream is = new FileInputStream(tmp);
        int j = IOUtils.readInt(is);
        is.close();
        Assert.assertEquals(j, i);

    }
}
