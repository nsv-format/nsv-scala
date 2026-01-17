package org.nsvformat

import scala.collection.mutable.ArrayBuffer

object Util {
  def spill[T](seqseq: Seq[Seq[T]], marker: T): Seq[T] = {
    val seq = ArrayBuffer[T]()
    for (row <- seqseq) {
      for (item <- row) {
        seq += item
      }
      seq += marker
    }
    seq.toSeq
  }

  def unspill[T](seq: Seq[T], marker: T): Seq[Seq[T]] = {
    val seqseq = ArrayBuffer[Seq[T]]()
    var row = ArrayBuffer[T]()
    for (item <- seq) {
      if (item != marker) {
        row += item
      } else {
        seqseq += row.toSeq
        row = ArrayBuffer[T]()
      }
    }
    seqseq.toSeq
  }
}