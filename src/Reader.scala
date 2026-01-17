package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class Reader(reader: java.io.Reader) extends Iterator[Seq[String]] {
  private val lineBuffer = new StringBuilder
  private val rowBuffer = ArrayBuffer[String]()
  private var cachedRow: Option[Seq[String]] = None

  private def tryReadLine(): Option[String] = {
    var c = reader.read()

    while (c != -1 && c != '\n') {
      lineBuffer.append(c.toChar)
      c = reader.read()
    }

    if (c == '\n') {
      val line = lineBuffer.toString
      lineBuffer.clear()
      Some(line)
    } else {
      None // EOF, line incomplete, preserve lineBuffer for next call
    }
  }

  @scala.annotation.tailrec
  private def tryReadRow(): Option[Seq[String]] = {
    tryReadLine() match {
      case Some(line) =>
        if (line.isEmpty) {
          // Row terminator found
          val row = rowBuffer.toSeq
          rowBuffer.clear()
          Some(row)
        } else {
          // Cell complete, add to row
          rowBuffer += Nsv.unescape(line)
          tryReadRow()  // tail recursive call
        }
      case None =>
        // EOF, row incomplete, preserve rowBuffer for next call
        None
    }
  }

  def hasNext: Boolean = {
    if (cachedRow.isDefined) return true
    cachedRow = tryReadRow()
    cachedRow.isDefined
  }

  def next(): Seq[String] = {
    if (!hasNext) throw new NoSuchElementException
    val result = cachedRow.get
    cachedRow = None
    result
  }
}

object Reader {
  def fromFile(file: java.io.File): Reader =
    new Reader(new java.io.FileReader(file))

  def fromPath(path: java.nio.file.Path): Reader =
    new Reader(new java.io.FileReader(path.toFile))
}
