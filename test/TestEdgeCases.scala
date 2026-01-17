package org.nsvformat

class TestEdgeCases extends munit.FunSuite {
  test("long strings") {
    val longString = (0x0B until 0x110000)
      .filterNot(cp => cp >= 0xD800 && cp <= 0xDFFF) // skip surrogates
      .map(_.toChar)
      .mkString

    val data = Seq(
      Seq("normal", longString),
      Seq(longString, "normal")
    )

    val encoded = Nsv.encode(data)
    val decoded = Nsv.decode(encoded)
    assertEquals(decoded, data)
  }

  test("special characters") {
    val data = Seq(
      Seq("field with spaces", "field,with,commas", "field\twith\ttabs"),
      Seq("field\"with\"quotes", "field'with'quotes", "field\\with\\backslashes"),
      Seq("field\nwith\nnewlines", "field, just field")
    )

    val encoded = Nsv.encode(data)
    val decoded = Nsv.decode(encoded)
    assertEquals(decoded, data)
  }

  test("trailing backslash handling") {
    // Dangling backslash should be stripped per spec
    val input = "yo\nshouln'ta\nbe\ndoing\nthis\\\n\n\\\nor\n\\\nshould\n\\\nya\n\n"
    val expected = Seq(
      Seq("yo", "shouln'ta", "be", "doing", "this"),
      Seq("", "or", "", "should", "", "ya")
    )
    val decoded = Nsv.decode(input)
    assertEquals(decoded, expected)
  }

  test("unrecognized escape sequences") {
    // Per spec, unrecognized sequences pass through with literal backslash
    val input = "\\x\\y\\z\n\n"
    val expected = Seq(Seq("\\x\\y\\z"))
    val decoded = Nsv.decode(input)
    assertEquals(decoded, expected)
  }

  test("empty string vs empty cell token") {
    // Empty cell token becomes empty string
    assertEquals(Nsv.unescape("\\"), "")
    // Empty string encodes to empty cell token
    assertEquals(Nsv.escape(""), "\\")
  }

  test("multiple consecutive empty rows") {
    val data = Seq(
      Seq(),
      Seq("r2c1", "r2c2"),
      Seq(),
      Seq(),
      Seq("r5c1", "r5c2", "r5c3"),
      Seq()
    )

    val encoded = Nsv.encode(data)
    val decoded = Nsv.decode(encoded)
    assertEquals(decoded, data)
  }

  test("escape explosion") {
    // Repeated encoding should increase backslashes
    val original = "\\"
    val once = Nsv.escape(original)
    assertEquals(once, "\\\\")

    val twice = Nsv.escape(once)
    assertEquals(twice, "\\\\\\\\")

    // And unescape should reverse it
    assertEquals(Nsv.unescape(twice), once)
    assertEquals(Nsv.unescape(once), original)
  }

  test("newline explosion") {
    val original = "\n"
    val once = Nsv.escape(original)
    assertEquals(once, "\\n")

    val twice = Nsv.escape(once)
    assertEquals(twice, "\\\\n")

    assertEquals(Nsv.unescape(twice), once)
    assertEquals(Nsv.unescape(once), original)
  }
}
