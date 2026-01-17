package org.nsvformat

class TestSpill extends munit.FunSuite {
  test("spill invertibility") {
    val cases = Seq(
      (Seq(), ""),
      (Seq(Seq()), ""),
      (Seq(Seq(), Seq()), ""),
      (Seq(Seq("a")), ""),
      (Seq(Seq("a", "b"), Seq("c")), ""),
      (Seq(Seq("a"), Seq(), Seq("b")), "")
    )

    cases.foreach { case (seqseq, marker) =>
      val spilled = Util.spill(seqseq, marker)
      val recovered = Util.unspill(spilled, marker)
      assertEquals(recovered, seqseq)
    }
  }

  test("unspill invertibility") {
    val cases = Seq(
      (Seq(), ""),
      (Seq(""), ""),
      (Seq("", ""), ""),
      (Seq("a", ""), ""),
      (Seq("a", "b", "", "c", ""), ""),
      (Seq("a", "", "", "b", ""), "")
    )

    cases.foreach { case (seq, marker) =>
      val unspilled = Util.unspill(seq, marker)
      val recovered = Util.spill(unspilled, marker)
      assertEquals(recovered, seq)
    }
  }

  test("spill with different types") {
    // spill[String, '']
    val strings2d = Seq(Seq("a", "b"), Seq("c"))
    val result1 = Util.spill(strings2d, "")
    val expected1 = Seq("a", "b", "", "c", "")
    assertEquals(result1, expected1)

    // spill[Char, '\n']
    val strings = Seq("hello", "world")
    val result2 = Util.spill(strings.map(_.toSeq), '\n')
    val expected2 = "hello\nworld\n".toSeq
    assertEquals(result2, expected2)

    // spill[Int, -1]
    val ints = Seq(Seq(1, 2), Seq(3))
    val result3 = Util.spill(ints, -1)
    val expected3 = Seq(1, 2, -1, 3, -1)
    assertEquals(result3, expected3)
  }
}
