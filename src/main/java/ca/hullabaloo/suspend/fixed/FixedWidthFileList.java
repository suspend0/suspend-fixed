package ca.hullabaloo.suspend.fixed;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractList;
import java.util.RandomAccess;

import static ca.hullabaloo.suspend.fixed.Guava.checkNotNull;

/**
 * A high-performance, immutable, random-access {@code List} implementation on top of
 * a {@link FixedWidthFile}.
 * <p></p>
 * Does not permit null elements.
 * <p/>
 * This class maps the whole file into memory.
 */
public class FixedWidthFileList extends AbstractList<ByteBuffer> implements RandomAccess, Closeable {
  private final Closeable closeable;
  private final ByteBuffer source;
  private final int recordSize;
  private final int listSize;

  public FixedWidthFileList(File file) throws IOException {
    this(new RandomAccessFile(file, "r"));
  }

  private FixedWidthFileList(RandomAccessFile raf) throws IOException {
    this(raf, raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, raf.length()));
  }

  //@VisibleForTesting
  FixedWidthFileList(Closeable closeable, ByteBuffer buffer) {
    this.closeable = checkNotNull(closeable);
    this.recordSize = buffer.getInt();
    this.source = buffer.slice(); // so position 0 is the first data element
    this.listSize = this.source.remaining() / this.recordSize;
  }

  @Override public ByteBuffer get(int index) {
    rangeCheck(index);
    ByteBuffer result = this.source.duplicate();
    result.position(index * recordSize);
    result.limit(result.position() + recordSize);
    return result.slice();
  }

  @Override public int size() {
    return listSize;
  }

  @Override public void close() throws IOException {
    this.closeable.close();
  }

  private void rangeCheck(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size();
  }
}
