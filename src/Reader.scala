package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class Reader(reader: java.io.Reader) extends Iterator[Seq[String]] {
  private val lineBuffer = new StringBuilder
  private var eof = false

  private def readLine(): String = {
    if (eof) return null
    lineBuffer.clear()
    var c = reader.read()

    while (c != -1 && c != '\n') {
      lineBuffer.append(c.toChar)
      c = reader.read()
    }

    if (c == -1) {
      eof = true
      if (lineBuffer.isEmpty) null else lineBuffer.toString
    } else {
      lineBuffer.append('\n')
      lineBuffer.toString
    }
  }

  def hasNext: Boolean = !eof

  def next(): Seq[String] = {
    val acc = ArrayBuffer[String]()
    var line = readLine()

    while (line != null) {
      if (line == "\n") {
        return acc.toSeq
      }
      val stripped = if (line.endsWith("\n")) line.dropRight(1) else line
      acc += Nsv.unescape(stripped)
      line = readLine()
    }

    if (acc.nonEmpty) {
      acc.toSeq
    } else {
      throw new NoSuchElementException
    }
  }
}

object Reader {
  def fromFile(file: java.io.File): Reader =
    new Reader(new java.io.FileReader(file))

  def fromPath(path: java.nio.file.Path): Reader =
    new Reader(new java.io.FileReader(path.toFile))
}
