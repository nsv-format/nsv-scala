package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class Reader(reader: java.io.Reader) extends Iterator[Seq[String]] {
  private val lineBuffer = new StringBuilder
  private var cachedRow: Option[Seq[String]] = None
  private var exhausted = false

  private def readLine(): Option[String] = {
    lineBuffer.clear()
    var c = reader.read()

    while (c != -1 && c != '\n') {
      lineBuffer.append(c.toChar)
      c = reader.read()
    }

    if (c == -1 && lineBuffer.isEmpty) None else Some(lineBuffer.toString)
  }

  private def readRow(): Option[Seq[String]] = {
    val acc = ArrayBuffer[String]()
    var line = readLine()

    while (line.isDefined) {
      val lineStr = line.get
      if (lineStr.isEmpty) {
        return Some(acc.toSeq)
      }
      acc += Nsv.unescape(lineStr)
      line = readLine()
    }

    if (acc.nonEmpty) Some(acc.toSeq) else None
  }

  def hasNext: Boolean = {
    if (cachedRow.isDefined) return true
    if (exhausted) return false

    cachedRow = readRow()
    if (cachedRow.isEmpty) {
      exhausted = true
      false
    } else {
      true
    }
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
