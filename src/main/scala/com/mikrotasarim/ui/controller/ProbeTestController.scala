package com.mikrotasarim.ui.controller

import com.mikrotasarim.ui.model.ProbeTestCase

import scala.StringBuilder
import scala.collection.mutable

object ProbeTestController {

  val testCases = Seq(
    new ProbeTestCase("Current Control Test", currentControlTest),
    new ProbeTestCase("Serial Interface Test", serialInterfaceTest),
    new ProbeTestCase("Memory Test", memoryTest),
    new ProbeTestCase("Power Consumption Test", powerConsumptionTest),
    new ProbeTestCase("Timing Generator Test", timingGeneratorTest),
    new ProbeTestCase("Output Timing Generator Test", outputTimingGeneratorTest),
    new ProbeTestCase("Output Channel Test", outputChannelTest),
    new ProbeTestCase("ADC Channel Functionality Test", adcChannelFunctionalityTest),
    new ProbeTestCase("PGA Functionality Test", pgaFunctionalityTest),
    new ProbeTestCase("Input Driver Test", inputDriverTest),
    new ProbeTestCase("PGA Gain Test", pgaGainTest),
    new ProbeTestCase("ADC Channel Linearity Test", adcChannelLinearityTest),
    new ProbeTestCase("ADC Channel Noise Test", adcChannelNoiseTest),
    new ProbeTestCase("ROIC Programming Test", roicProgrammingTest)
  )

  val fc = FpgaController

  def dc = fc.deviceController

  val psc = PowerSourceController
  val rvc = ReferenceValueController

  private def reset(): Unit = {
    dc.putAsicOnReset()
    dc.putFpgaOnReset()
    dc.takeFpgaOffReset()
    dc.takeAsicOffReset()
  }

  private def currentControlTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    val current = psc.measureCurrent()
    if (rvc.checkCurrent(current)) {
      (true, "")
    } else {
      (false, current + " A\n")
    }
  }

  private def serialInterfaceTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.writeToAsicMemoryTop(0x59, 0xabcd)
    val read = dc.readFromAsicMemoryTop(0x59)
    if (read == 0xabcd) {
      (true, "")
    } else {
      (false, read + "read\n")
    }
  }

  private def memoryTest(): (Boolean, String) = {
    import spire.implicits._
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    val topAddresses = (0 to 255).filterNot(p => p == 0x6d || p == 0x74)
    val botAddresses = 0 to 127
    for (i <- topAddresses) dc.writeToAsicMemoryTop(i, 0)
    for (i <- botAddresses) dc.writeToAsicMemoryBot(i, 0)
    val errors = new StringBuilder()
    for (i <- topAddresses) {
      dc.writeToAsicMemoryTop(i, 0xffff)
      val read = dc.readFromAsicMemoryTop(i)
      for (j <- 0 to 15) {
        if ((read / (2 pow j)) % 2 != 1) {
          errors.append("Top (" + i + ", " + j + ") failed to toggle from 0 to 1\n")
        }
      }
    }
    for (i <- botAddresses) {
      dc.writeToAsicMemoryBot(i, 0xffff)
      val read = dc.readFromAsicMemoryTop(i)
      for (j <- 0 to 15) {
        if ((read / (2 pow j)) % 2 != 1) {
          errors.append("Bottom (" + i + ", " + j + ") failed to toggle from 0 to 1\n")
        }
      }
    }
    for (i <- topAddresses) {
      dc.writeToAsicMemoryTop(i, 0)
      val read = dc.readFromAsicMemoryTop(i)
      for (j <- 0 to 15) {
        if ((read / (2 pow j)) % 2 != 0) {
          errors.append("Top (" + i + ", " + j + ") failed to toggle from 1 to 0\n")
        }
      }
    }
    for (i <- botAddresses) {
      dc.writeToAsicMemoryBot(i, 0)
      val read = dc.readFromAsicMemoryBot(i)
      for (j <- 0 to 15) {
        if ((read / (2 pow j)) % 2 != 0) {
          errors.append("Bottom (" + i + ", " + j + ") failed to toggle from 1 to 0\n")
        }
      }
    }
    (errors.toString().isEmpty, errors.toString())
  }

  private def powerConsumptionTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def timingGeneratorTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def outputTimingGeneratorTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def outputChannelTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelFunctionalityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def pgaFunctionalityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def inputDriverTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def pgaGainTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelLinearityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelNoiseTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def roicProgrammingTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithRoic)
    (false, "Not Implemented Yet\n")
  }

  def runAll(): Unit = {
    for (testCase <- testCases) testCase.runTest()
  }
}