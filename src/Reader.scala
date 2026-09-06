package org.nsvformat

import scala.collection.mutable.ArrayBuffer

class Reader(reader: java.io.Reader, bufferSize: Int = 8192) extends Iterator[Seq[String]] {
  private val lineBuffer = new StringBuilder
  private val rowBuffer = ArrayBuffer[String]()
  private var cachedRow: Option[Seq[String]] = None

  private val buf = new Array[Char](bufferSize)
  private var bufPos = 0
  private var bufLen = 0

  private def tryReadLine(): Option[String] = {
    while (true) {
      var i = bufPos
      while (i < bufLen) {
        if (buf(i) == '\n') {
          // Line complete, return
          lineBuffer.appendAll(buf, bufPos, i - bufPos)
          bufPos = i + 1
          val line = lineBuffer.toString
          lineBuffer.clear()
          return Some(line)
        }
        i += 1
      }
      // Keep reading
      lineBuffer.appendAll(buf, bufPos, bufLen - bufPos)
      bufLen = reader.read(buf, 0, buf.length)
      bufPos = 0
      if (bufLen == -1) {
        // Incomplete line at EOF, preserve lineBuffer for next call
        bufLen = 0
        return None
      }
    }
    None // unreachable
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
    new Reader(new java.io.FileReader(file))

  def fromPath(path: java.nio.file.Path): Reader =
    new Reader(new java.io.FileReader(path.toFile))
}
