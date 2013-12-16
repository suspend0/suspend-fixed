package ca.hullabaloo.suspend.fixed;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collections;

public class FixedWidthFileListTest {
  public static FixedWidthFileList build(long[] values) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      FixedWidthFile f = new FixedWidthFile(8, Channels.newChannel(bos));
      ByteBuffer bb = ByteBuffer.allocate(8);
      for (long val : values) {
        f.append(bb.putLong(0, val)).flip();
      }
      f.close();
    } catch (IOException e) {
      // We're in memory, so one wouldn't expect this to happen.
      // Catch-and-rethrow for the convenience of setting as member
      throw new Error(e);
    }

    byte[] bytes = bos.toByteArray();

    return new FixedWidthFileList(new NullCloseable(), ByteBuffer.wrap(bytes));
  }

  private long[] values = {99, 8883, 1343413431435L, 0, 3387};
  private FixedWidthFileList list = build(values);

  @Test public void getByIndex() throws IOException {
    Assert.assertEquals(values[1], list.get(1).getLong());
  }

  @Test public void iteration() throws IOException {
    int i = 0;
    for (ByteBuffer item : list) {
      Assert.assertEquals(values[i++], item.getLong());
    }
  }

  @Test public void contains() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(8).putLong(1343413431435L);
    bb.flip();
    Assert.assertTrue(list.contains(bb));
  }

  @Test public void containsDoesNotThrowNpe() throws IOException {
    Assert.assertFalse(list.contains(null));
  }

  @Test public void binarySearchForLargeValue() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(8).putLong(1343413431435L);
    bb.flip();

    Arrays.sort(values);
    list = build(values);
    Assert.assertEquals(4, Collections.binarySearch(list, bb));
  }

  @Test public void binarySearchForLargeMissingValue() throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(8).putLong(13434131435L);
    bb.flip();

    Arrays.sort(values);
    list = build(values);
    Assert.assertEquals(-5, Collections.binarySearch(list, bb));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void set() {
    list.set(0, ByteBuffer.allocate(8));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void getOutOfBounds() {
    list.get(33);
  }

  private static final class NullCloseable implements Closeable {
    @Override
    public void close() throws IOException {
      // no-op
    }
  }
}

