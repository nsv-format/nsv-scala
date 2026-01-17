package org.nsvformat

class Writer(writer: java.io.Writer) {
  def writeRow(row: Seq[String]): Unit = {
    row.foreach { cell =>
      writer.write(Nsv.escape(cell))
      writer.write('\n')
    }
    writer.write('\n')
  }

  def writeRows(rows: Seq[Seq[String]]): Unit =
    rows.foreach(writeRow)
}

object Writer {
  def fromFile(file: java.io.File): Writer =
    new Writer(new java.io.BufferedWriter(new java.io.FileWriter(file)))

  def fromPath(path: java.nio.file.Path): Writer =
    new Writer(new java.io.BufferedWriter(new java.io.FileWriter(path.toFile)))
}
