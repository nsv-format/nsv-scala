package org.nsvformat

import scala.collection.mutable.ArrayBuffer

object Nsv {
  def loads(s: String): Seq[Seq[String]] = {
    val data = ArrayBuffer[ArrayBuffer[String]]()
    var row = ArrayBuffer[String]()
    var start = 0
    for ((c, pos) <- s.zipWithIndex) {
      if (c == '\n') {
        if (pos - start >= 1) {
          row += unescape(s.substring(start, pos))
        } else {
          data += row
          row = ArrayBuffer[String]()
        }
        start = pos + 1
      }
    }
    data.map(_.toSeq).toSeq
  }

  def dumps(data: Seq[Seq[String]]): String = {
    val lines = ArrayBuffer[String]()
    for (row <- data) {
      for (cell <- row) {
        lines += escape(cell)
      }
      lines += ""
    }
    lines.map(_ + "\n").mkString
  }

  private def escape(s: String): String =
    if (s == "") {
      "\\"
    } else if (s.contains('\n') || s.contains('\\')) {
      s.replace("\\", "\\\\").replace("\n", "\\n")
    } else {
      s
    }

  private def unescape(s: String): String = {
    if (s == "\\") {
      return ""
    }
    if (!s.contains('\\')) {
      return s
    }
    val out = new StringBuilder
    var escaped = false
    for (c <- s) {
      if (escaped) {
        if (c == 'n') {
          out.append('\n')
        } else if (c == '\\') {
          out.append('\\')
        } else {
          out.append('\\')
          out.append(c)
        }
        escaped = false
      } else {
        if (c == '\\') {
          escaped = true
        } else {
          out.append(c)
        }
      }
    }
    out.toString
  }
}
