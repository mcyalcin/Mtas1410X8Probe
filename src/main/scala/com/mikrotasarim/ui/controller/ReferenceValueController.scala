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
  val linearityRef = for (i <- 0 until 410) yield linearityInput.next().toDouble
  
  val noiseThreshold = noiseInput.next().toDouble

  def checkAdcLinearity(out: Seq[Seq[Long]]): (Boolean, String) = {
    var pass = true
    val values = new StringBuilder
    for (i <- linearityRef.indices) {
      values.append(linearityRef(i) + " -> \t")
      for (c <- 0 to 7) {
        if (Math.abs(out(i)(c)-linearityRef(i)) > linearityMargin) {
          values.append(out(i)(c) + "* \t")
          pass = false
        } else {
          values.append(out(i)(c) + " \t")
        }
      }
      values.append("\n")
    }
    (pass, values.toString())
  }

  def checkPower(powerSeq: Seq[Double]): Seq[Boolean] = {
    for (i <- powerRef.indices) yield Math.abs(powerSeq(i)-powerRef(i)) < powerMargin
  }

  def checkCurrent(current: Double): Boolean = {
    Math.abs(current - currentRef) < currentMargin
  }
}
