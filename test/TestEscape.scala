package org.nsvformat

class TestEscape extends munit.FunSuite {
  test("escape unescape invertibility") {
    val testCases = Seq(
      "",
      "hello",
      "hello\nworld",
      "backslash\\here",
      "both\nand\\here",
      "multiple\n\n\nlines",
      "multiple\\\\\\backslashes"
    )

    testCases.foreach { s =>
      val escaped = Nsv.escape(s)
      val recovered = Nsv.unescape(escaped)
      assertEquals(recovered, s, s"Failed for string: $s")
    }
  }

  test("escape empty string") {
    assertEquals(Nsv.escape(""), "\\")
  }

  test("unescape empty cell token") {
    assertEquals(Nsv.unescape("\\"), "")
  }

  test("escape newline") {
    assertEquals(Nsv.escape("hello\nworld"), "hello\\nworld")
  }

  test("escape backslash") {
    assertEquals(Nsv.escape("hello\\world"), "hello\\\\world")
  }

  test("escape both") {
    assertEquals(Nsv.escape("hello\n\\world"), "hello\\n\\\\world")
  }

  test("unescape newline") {
    assertEquals(Nsv.unescape("hello\\nworld"), "hello\nworld")
  }

  test("unescape backslash") {
    assertEquals(Nsv.unescape("hello\\\\world"), "hello\\world")
  }

  test("unescape both") {
    assertEquals(Nsv.unescape("hello\\n\\\\world"), "hello\n\\world")
  }

  test("unescape unrecognized sequence passes through") {
    assertEquals(Nsv.unescape("hello\\xworld"), "hello\\xworld")
  }

  test("escape seqseq using map") {
    val seqseq = Seq(Seq("a", "b\n"), Seq("c\\d", ""))
    val expected = Seq(Seq("a", "b\\n"), Seq("c\\\\d", "\\"))
    val result = seqseq.map(_.map(Nsv.escape))
    assertEquals(result, expected)

    val recovered = result.map(_.map(Nsv.unescape))
    assertEquals(recovered, seqseq)
  }
}
