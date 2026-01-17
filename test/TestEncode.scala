package org.nsvformat

class TestEncode extends munit.FunSuite {
  test("encode empty") {
    assertEquals(Nsv.encode(Seq()), "")
  }

  test("encode single empty row") {
    assertEquals(Nsv.encode(Seq(Seq())), "\n")
  }

  test("encode two empty rows") {
    assertEquals(Nsv.encode(Seq(Seq(), Seq())), "\n\n")
  }

  test("encode basic") {
    val input = Seq(Seq("a", "b", "c"), Seq("d", "e", "f"))
    val expected = "a\nb\nc\n\nd\ne\nf\n\n"
    assertEquals(Nsv.encode(input), expected)
  }

  test("encode empty fields") {
    val input = Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3"))
    val expected = "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n"
    assertEquals(Nsv.encode(input), expected)
  }

  test("encode decode invertibility") {
    val testCases = Seq(
      Seq(),
      Seq(Seq()),
      Seq(Seq(), Seq()),
      Seq(Seq("a", "b", "c"), Seq("d", "e", "f")),
      Seq(Seq("r1c1", "", "r1c3"), Seq("r2c1", "", "r2c3")),
      Seq(Seq("r1c1", "r1c2"), Seq(), Seq("r3c1", "r3c2")),
      Seq(Seq(), Seq("r2c1", "r2c2"), Seq("r3c1", "r3c2")),
      Seq(Seq("r1c1", "r1c2"), Seq("r2c1", "r2c2"), Seq()),
      Seq(
        Seq(),
        Seq("r2c1", "r2c2"),
        Seq(),
        Seq(),
        Seq("r5c1", "r5c2", "r5c3"),
        Seq()
      ),
      Seq(
        Seq("line1\nline2", "r1c2", "r1c3"),
        Seq("anotherline1\nline2\nline3", "r2c2")
      ),
      Seq(
        Seq("\\n", "\\\n", "\\\\n"),
        Seq("\\\\", "\n\n")
      ),
      Seq(Seq(""))
    )

    testCases.foreach { data =>
      val encoded = Nsv.encode(data)
      val decoded = Nsv.decode(encoded)
      assertEquals(decoded, data)
    }
  }

  test("decode encode invertibility for valid nsv strings") {
    val testCases = Seq(
      "",
      "\n",
      "\n\n",
      "a\nb\nc\n\nd\ne\nf\n\n",
      "r1c1\n\\\nr1c3\n\nr2c1\n\\\nr2c3\n\n",
      "r1c1\nr1c2\n\n\nr3c1\nr3c2\n\n",
      "\nr2c1\nr2c2\n\nr3c1\nr3c2\n\n",
      "r1c1\nr1c2\n\nr2c1\nr2c2\n\n\n",
      "\\\n\n"
    )

    testCases.foreach { s =>
      val decoded = Nsv.decode(s)
      val encoded = Nsv.encode(decoded)
      assertEquals(encoded, s)
    }
  }
}
