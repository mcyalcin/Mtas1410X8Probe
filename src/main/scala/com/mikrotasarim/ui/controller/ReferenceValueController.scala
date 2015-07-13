package com.mikrotasarim.ui.controller

//import scala.io.Source

object ReferenceValueController {

//  val input = Source fromFile "ref.conf" getLines()

  val noiseTreshold = 100

  def checkAdcLinearity(out: Seq[Seq[Long]]): (Boolean, String) = (false, "Not Implemented Yet\n")

  def checkPower(currentSeq: Seq[Double]): Seq[Boolean] = {
    currentSeq.map(_ => false) // TODO: Implement
  }

  def checkCurrent(current: Double): Boolean = {
    false // TODO: Implement
  }
}
