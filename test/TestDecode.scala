package org.nsvformat

class TestDecode extends munit.FunSuite {
  test("decode empty") {
    assertEquals(Nsv.decode(""), Seq())
  }

  test("decode single empty row") {
    assertEquals(Nsv.decode("\n"), Seq(Seq()))
  }

  test("decode two empty rows") {
    assertEquals(Nsv.decode("\n\n"), Seq(Seq(), Seq()))
  }

  test("decode basic") {
    val input = "a\nb\nc\n\nd\ne\nf\n\n"
    val expected = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode empty fields") {
    val input = "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n"
    val expected = Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3"))
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode empty sequence middle") {
    val input = "r1c1\nr1c2\n\n\nr3c1\nr3c2\n\n"
    val expected = Seq(Seq("r1c1", "r1c2"), Seq(), Seq("r3c1", "r3c2"))
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode empty sequence start") {
    val input = "\nr2c1\nr2c2\n\nr3c1\nr3c2\n\n"
    val expected = Seq(Seq(), Seq("r2c1", "r2c2"), Seq("r3c1", "r3c2"))
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode empty sequence end") {
    val input = "r1c1\nr1c2\n\nr2c1\nr2c2\n\n\n"
    val expected = Seq(Seq("r1c1", "r1c2"), Seq("r2c1", "r2c2"), Seq())
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode multiple empty sequences") {
    val input = "\nr2c1\nr2c2\n\n\n\nr5c1\nr5c2\nr5c3\n\n\n"
    val expected = Seq(
      Seq(),
      Seq("r2c1", "r2c2"),
      Seq(),
      Seq(),
      Seq("r5c1", "r5c2", "r5c3"),
      Seq()
    )
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode multiline encoded") {
    val input = "line1\\nline2\nr1c2\nr1c3\n\nanotherline1\\nline2\\nline3\nr2c2\n\n"
    val expected = Seq(
      Seq("line1\nline2", "r1c2", "r1c3"),
      Seq("anotherline1\nline2\nline3", "r2c2")
    )
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode escape edge cases") {
    val input = "\\\\n\n\\\\\\n\n\\\\\\\\n\n\n\\\\\\\\\n\\n\\n\n\n"
    val expected = Seq(
      Seq("\\n", "\\\n", "\\\\n"),
      Seq("\\\\", "\n\n")
    )
    assertEquals(Nsv.decode(input), expected)
  }

  test("decode one one") {
    val input = "\\\n\n"
    val expected = Seq(Seq(""))
    assertEquals(Nsv.decode(input), expected)
  }
}
