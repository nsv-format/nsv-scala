package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class Reader(reader: java.io.Reader) extends Iterator[Seq[String]] {
  private val lineBuffer = new StringBuilder
  private val rowBuffer = ArrayBuffer[String]()
  private var cachedRow: Option[Seq[String]] = None

  @scala.annotation.tailrec
  private def tryReadLine(): Option[String] =
    reader.read() match {
      case -1 => None // Incomplete line at EOF, preserve lineBuffer for next call
      case '\n' => // Line complete, return
        val line = lineBuffer.toString
        lineBuffer.clear()
        Some(line)
      case c =>  // Keep reading
        lineBuffer.append(c.toChar)
        tryReadLine()
    }

  @scala.annotation.tailrec
  private def tryReadRow(): Option[Seq[String]] =
    tryReadLine() match {
      case None => None // Incomplete row at EOF, preserve rowBuffer for next call
      case Some("") => // Row complete, return
        val row = rowBuffer.toSeq
        rowBuffer.clear()
        Some(row)
      case Some(line) => // Cell complete, keep reading
        rowBuffer += Nsv.unescape(line)
        tryReadRow()
    }

  def hasNext: Boolean = {
    if (cachedRow.isEmpty) {
      cachedRow = tryReadRow()
    }
    cachedRow.isDefined
  }

  def next(): Seq[String] =
    if (hasNext) {
      val result = cachedRow.get
      cachedRow = None
      result
    } else {
      throw new NoSuchElementException
    }
}

object Reader {
  def fromFile(file: java.io.File): Reader =
    new Reader(new java.io.BufferedReader(new java.io.FileReader(file)))

  def fromPath(path: java.nio.file.Path): Reader =
    new Reader(new java.io.BufferedReader(new java.io.FileReader(path.toFile)))
}
