package ca.hullabaloo.suspend.fixed;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FixedWidthFileTest {
  @ClassRule public static TemporaryFolder tmp = new TemporaryFolder();

  @Test public void create() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(8);
    File file = tmp.newFile();

    FixedWidthFile f = FixedWidthFile.create(8, file);
    for (int i = 0; i < 10; i++) {
      f.append(bb.putLong(0, i)).flip();
    }
    f.close();
    Assert.assertEquals(8 * 10 + 4, file.length());
  }

  @Test(expected = IllegalArgumentException.class)
  public void wrongSizeRecord() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(8);
    bb.putInt(9);
    File file = tmp.newFile();
    FixedWidthFile f = FixedWidthFile.create(8, file);
    f.append(bb).flip();
    f.close();
  }
}
