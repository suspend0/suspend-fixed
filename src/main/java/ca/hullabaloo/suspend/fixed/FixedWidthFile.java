package ca.hullabaloo.suspend.fixed;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import static ca.hullabaloo.suspend.fixed.Guava.checkArgument;

/**
 * A file that is made up of fixed-with records.
 */
public class FixedWidthFile implements Closeable {
  public static FixedWidthFile create(int recordSize, File file) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "rw");
    raf.setLength(0);
    return new FixedWidthFile(recordSize, raf.getChannel());
  }

  private final int recordSize;
  private final WritableByteChannel target;

  FixedWidthFile(int recordSize, WritableByteChannel target) throws IOException {
    checkArgument(recordSize > 0, "must have a record size greater than zero");
    this.recordSize = recordSize;
    this.target = target;
    this.target.write(header());
  }

  private ByteBuffer header() {
    return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(0, recordSize);
  }

  /**
   * Appends the remaining content of the provided buffer to the file.  When this method returns,
   * the provided buffer will be exhausted ({@link java.nio.ByteBuffer#hasRemaining()}
   * will be <code>false</code>)
   *
   * @return the passed buffer, so you can call {@link java.nio.ByteBuffer#flip()}
   * @throws IllegalArgumentException if the buffer's remaining bytes does not match
   *                                  the configured record size
   */
  public ByteBuffer append(ByteBuffer record) throws IOException {
    checkArgument(record.remaining() == recordSize,
        "Expected record of size %s, got %s", recordSize, record.remaining());

    // always try once
    target.write(record);

    // hopefully JVM notices that this is always false
    while (record.hasRemaining()) {
      target.write(record);
    }

    return record;
  }

  @Override public void close() throws IOException {
    this.target.close();
  }
}
