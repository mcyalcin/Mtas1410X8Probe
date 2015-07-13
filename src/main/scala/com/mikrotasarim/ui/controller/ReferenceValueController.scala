package com.mikrotasarim.ui.controller

import scala.collection.immutable.IndexedSeq

object ReferenceValueController {
  def checkAdcLinearity(out: Seq[Seq[Long]]): (Boolean, String) = (false, "Not Implemented Yet\n")

  def checkPower(currentSeq: Seq[Double]): Seq[Boolean] = {
    currentSeq.map(_ => false) // TODO: Implement
  }

  def checkCurrent(current: Double): Boolean = {
    false // TODO: Implement
  }
}
