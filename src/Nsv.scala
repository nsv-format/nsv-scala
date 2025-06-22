package org.nsvformat

final case class Nsv(
  metadata: String,
  data: Seq[Seq[String]],
) {
  def asString = s"$metadata${Nsv.Separator}${Nsv.dataToString(data)}"
}

object Nsv {
  def fromString(s: String): Nsv = {
    val Array(metadata, data) = s.split(Separator, 2)
    Nsv(metadata, dataFromString(data))
  }

  private val Separator = "\n---\n"

  private def dataFromString(s: String): Seq[Seq[String]] = {
    s.split("\n\n").toSeq.map(row => row.split("\n").toSeq.map(unescape))
  }

  private def dataToString(seq: Seq[Seq[String]]): String = {
    seq.map(row => row.map(escape).mkString("\n")).mkString("\n\n")
  }

  private def escape(s: String): String = {
    if (s == "") { "\\" } else { s.replace("\\", "\\\\").replace("\n", "\\n") }
  }

  private def unescape(s: String): String = {
    if (s == "\\") { "" } else { s.replace("\\n", "\n").replace("\\\\", "\\") }
  }
}
