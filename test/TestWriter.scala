package org.nsvformat

import java.io.StringWriter

class TestWriter extends munit.FunSuite {
  test("writer basic") {
    val data = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))
    val output = new StringWriter()
    val writer = new Writer(output)

    writer.writeRows(data)

    val expected = "a\nb\nc\n\nd\ne\nf\n\n"
    assertEquals(output.toString, expected)
  }

  test("writer incremental") {
    val output = new StringWriter()
    val writer = new Writer(output)

    writer.writeRow(Seq("a", "b", "c"))
    writer.writeRow(Seq("d", "e", "f"))

    val expected = "a\nb\nc\n\nd\ne\nf\n\n"
    assertEquals(output.toString, expected)
  }

  test("writer empty") {
    val output = new StringWriter()
    val writer = new Writer(output)
    writer.writeRows(Seq())
    assertEquals(output.toString, "")
  }

  test("writer single empty row") {
    val output = new StringWriter()
    val writer = new Writer(output)
    writer.writeRow(Seq())
    assertEquals(output.toString, "\n")
  }

  test("writer empty fields") {
    val data = Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3"))
    val output = new StringWriter()
    val writer = new Writer(output)

    writer.writeRows(data)

    val expected = "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n"
    assertEquals(output.toString, expected)
  }

  test("writer multiline encoded") {
    val data = Seq(
      Seq("line1\nline2", "r1c2", "r1c3"),
      Seq("anotherline1\nline2\nline3", "r2c2")
    )
    val output = new StringWriter()
    val writer = new Writer(output)

    writer.writeRows(data)

    val expected = "line1\\nline2\nr1c2\nr1c3\n\nanotherline1\\nline2\\nline3\nr2c2\n\n"
    assertEquals(output.toString, expected)
  }

  test("writer parity with encode") {
    val testCases = Seq(
      Seq(),
      Seq(Seq()),
      Seq(Seq(), Seq()),
      Seq(Seq("a", "b", "c"), Seq("d", "e", "f")),
      Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3")),
      Seq(Seq(""))
    )

    testCases.foreach { data =>
      val output = new StringWriter()
      val writer = new Writer(output)
      writer.writeRows(data)

      val fromWriter = output.toString
      val fromEncode = Nsv.encode(data)
      assertEquals(fromWriter, fromEncode)
    }
  }

  test("writer reader roundtrip") {
    val data = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))

    val output = new StringWriter()
    val writer = new Writer(output)
    writer.writeRows(data)

    val reader = new Reader(new java.io.StringReader(output.toString))
    val recovered = reader.toSeq

    assertEquals(recovered, data)
  }
}
