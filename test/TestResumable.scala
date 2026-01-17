package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class TestResumable extends munit.FunSuite {
  // Simulates a stream where data can be added dynamically
  class ResumableReader extends java.io.Reader {
    private val buffer = new StringBuilder
    private var pos = 0

    def addData(data: String): Unit = {
      buffer.append(data)
    }

    override def read(): Int = {
      if (pos < buffer.length) {
        val c = buffer.charAt(pos)
        pos += 1
        c.toInt
      } else {
        -1 // EOF for now
      }
    }

    override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
      if (len == 0) return 0
      val c = read()
      if (c == -1) return -1
      cbuf(off) = c.toChar
      1
    }

    override def close(): Unit = {}
  }

  test("resume after partial line at EOF") {
    val stream = new ResumableReader()
    stream.addData("a\nb")  // incomplete line
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, false, "No complete row yet")

    stream.addData("c\n\n")  // completes cell and row
    assertEquals(reader.hasNext, true, "Row completed after reading more")

    val row = reader.next()
    assertEquals(row, Seq("a", "bc"))
  }

  test("resume after partial row at EOF") {
    val stream = new ResumableReader()
    stream.addData("a\nb\n")  // one complete cell
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, false, "No complete row yet")

    stream.addData("c\n\n")  // second cell and row terminator
    assertEquals(reader.hasNext, true, "Row completed after reading more")

    val row = reader.next()
    assertEquals(row, Seq("a", "b", "c"))
  }

  test("resume across multiple EOFs") {
    val stream = new ResumableReader()
    stream.addData("a")
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, false, "Incomplete line")

    stream.addData("\n")
    assertEquals(reader.hasNext, false, "One cell, row incomplete")

    stream.addData("b")
    assertEquals(reader.hasNext, false, "Still incomplete")

    stream.addData("\n\n")
    assertEquals(reader.hasNext, true, "Row complete")

    val row = reader.next()
    assertEquals(row, Seq("a", "b"))
  }

  test("multiple rows with resumption") {
    val stream = new ResumableReader()
    stream.addData("a\n\n")
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, true)
    assertEquals(reader.next(), Seq("a"))

    stream.addData("b\n")
    assertEquals(reader.hasNext, false, "Second row incomplete")

    stream.addData("\n")
    assertEquals(reader.hasNext, true, "Second row complete")
    assertEquals(reader.next(), Seq("b"))

    assertEquals(reader.hasNext, false)
  }

  test("empty row with resumption") {
    val stream = new ResumableReader()
    stream.addData("\n")
    val reader = new Reader(stream)

    // Single \n is actually a complete empty row (empty line = row terminator)
    assertEquals(reader.hasNext, true, "Empty row complete")
    assertEquals(reader.next(), Seq())

    // Second \n would be another empty row, but we need to add it first
    assertEquals(reader.hasNext, false, "No more complete rows")
    stream.addData("\n")
    assertEquals(reader.hasNext, true, "Second empty row complete")
    assertEquals(reader.next(), Seq())
  }

  test("escaped content with resumption") {
    val stream = new ResumableReader()
    stream.addData("hel\\n")  // "hel\\nlo" split mid-escape
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, false)

    stream.addData("lo\n\n")
    assertEquals(reader.hasNext, true)
    assertEquals(reader.next(), Seq("hel\nlo"))
  }

  test("complete row arrives at once") {
    val stream = new ResumableReader()
    stream.addData("a\nb\n\n")
    val reader = new Reader(stream)

    assertEquals(reader.hasNext, true)
    assertEquals(reader.next(), Seq("a", "b"))

    stream.addData("c\n\n")
    assertEquals(reader.hasNext, true)
    assertEquals(reader.next(), Seq("c"))
  }
}
