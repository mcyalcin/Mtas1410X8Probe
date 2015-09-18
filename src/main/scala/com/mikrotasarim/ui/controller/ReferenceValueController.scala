package com.mikrotasarim.ui.controller

import scala.io.Source

object ReferenceValueController {

  val currentInput = Source fromFile "current.conf" getLines()
  val powerInput = Source fromFile "power.conf" getLines()
  val linearityInput = Source fromFile "linearity.conf" getLines()
  val noiseInput = Source fromFile "noise.conf" getLines()

  val currentMargin = currentInput.next().toDouble
  val currentRef = currentInput.next().toDouble
  
  val powerMargin = powerInput.next().toDouble
  val powerRef = for (i <- 0 until 25) yield powerInput.next().toDouble
  
  val linearityMargin = linearityInput.next().toDouble
  val linearityRef = for (i <- 0 until 4096) yield linearityInput.next().toDouble
  
  val noiseThreshold = noiseInput.next().toDouble

  def checkAdcLinearity(out: Seq[Seq[Long]]): (Boolean, String) = (false, "Not Implemented Yet\n")

  def checkPower(powerSeq: Seq[Double]): Seq[Boolean] = {
    for (i <- powerRef.indices) yield Math.abs(powerSeq(i)-powerRef(i)) < powerMargin
  }

  def checkCurrent(current: Double): Boolean = {
    Math.abs(current - currentRef) < currentMargin
  }
}
