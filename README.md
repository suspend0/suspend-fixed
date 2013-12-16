A memory-mapped file containing fixed-with records.
============

Includes an implementation of a random-access java.util.List on
top of a memory-mapped file.

  - Thread safety: readers are independent
  - No-copy: the file is mapped into memory and pointers returned
  - Compatibility: API is java.util.List

Usage
-------

```java
int recordLen = 8;
ByteBuffer bb = ...;
FixedWidthFile f = FixedWidthFile.create(recordLen, file);
for (int i = 0; i < 10; i++) {
  f.append(bb.putLong(0, i*7)).flip();
}

List<ByteBuffer> list = new FixedWidthFileList(file);
ByteBuffer bb = list.get(3);
bb.getLong(); // 21 (3*7);
```

Dependencies
-------
JUnit, otherwise none.

Status
-------
Core bits & read-only usage is stable.

