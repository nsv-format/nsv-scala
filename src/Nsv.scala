package org.nsvformat

import scala.collection.mutable.ArrayBuffer

object Nsv {
  def decode(s: String): Seq[Seq[String]] = {
    val data = ArrayBuffer[ArrayBuffer[String]]()
    var row = ArrayBuffer[String]()
    var start = 0
    var pos = s.indexOf('\n', start)
    while (pos >= 0) {
      if (pos > start) {
        row += unescape(s.substring(start, pos))
      } else {
        data += row
        row = ArrayBuffer[String]()
      }
      start = pos + 1
      pos = s.indexOf('\n', start)
    }
    if (start < s.length) {
      row += unescape(s.substring(start))
    }
    if (row.nonEmpty) {
      data += row
    }
    data.map(_.toSeq).toSeq
  }

  def encode(data: Seq[Seq[String]]): String = {
    val sb = new StringBuilder
    for (row <- data) {
      for (cell <- row) {
        sb.append(escape(cell))
        sb.append('\n')
      }
      sb.append('\n')
    }
    sb.toString
  }

  def escape(s: String): String =
    if (s == "") {
      "\\"
    } else if (s.contains('\n') || s.contains('\\')) {
      s.replace("\\", "\\\\").replace("\n", "\\n")
    } else {
      s
    }

  def unescape(s: String): String = {
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
        c match {
          case 'n' => out.append('\n')
          case '\\' => out.append('\\')
          case c =>
            out.append('\\')
            out.append(c)
        }
        escaped = false
      } else {
        c match {
          case '\\' => escaped = true
          case c => out.append(c)
        }
      }
    }
    out.toString
  }
}
