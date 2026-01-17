package org.nsvformat

import java.io.StringReader

class TestReader extends munit.FunSuite {
  test("reader basic") {
    val input = "a\nb\nc\n\nd\ne\nf\n\n"
    val reader = new Reader(new StringReader(input))

    val rows = reader.toSeq
    val expected = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))
    assertEquals(rows, expected)
  }

  test("reader incremental") {
    val input = "a\nb\nc\n\nd\ne\nf\n\n"
    val reader = new Reader(new StringReader(input))

    assertEquals(reader.hasNext, true)
    val first = reader.next()
    assertEquals(first, Seq("a", "b", "c"))

    assertEquals(reader.hasNext, true)
    val second = reader.next()
    assertEquals(second, Seq("d", "e", "f"))

    assertEquals(reader.hasNext, false)
  }

  test("reader empty") {
    val reader = new Reader(new StringReader(""))
    assertEquals(reader.hasNext, false)
  }

  test("reader single empty row") {
    val reader = new Reader(new StringReader("\n"))
    assertEquals(reader.hasNext, true)
    assertEquals(reader.next(), Seq())
    assertEquals(reader.hasNext, false)
  }

  test("reader empty fields") {
    val input = "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n"
    val reader = new Reader(new StringReader(input))
    val rows = reader.toSeq
    val expected = Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3"))
    assertEquals(rows, expected)
  }

  test("reader multiline encoded") {
    val input = "line1\\nline2\nr1c2\nr1c3\n\nanotherline1\\nline2\\nline3\nr2c2\n\n"
    val reader = new Reader(new StringReader(input))
    val rows = reader.toSeq
    val expected = Seq(
      Seq("line1\nline2", "r1c2", "r1c3"),
      Seq("anotherline1\nline2\nline3", "r2c2")
    )
    assertEquals(rows, expected)
  }

  test("reader parity with decode") {
    val testCases = Seq(
      "",
      "\n",
      "\n\n",
      "a\nb\nc\n\nd\ne\nf\n\n",
      "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n",
      "\\\n\n"
    )

    testCases.foreach { input =>
      val fromReader = new Reader(new StringReader(input)).toSeq
      val fromDecode = Nsv.decode(input)
      assertEquals(fromReader, fromDecode)
    }
  }
}
