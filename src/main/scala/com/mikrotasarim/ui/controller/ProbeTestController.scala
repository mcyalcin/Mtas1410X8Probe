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
    dc.initializeAsic()
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
    dc.initializeAsic()
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
    reset()
    dc.putAsicOnReset()
    val currentS1 = psc.measureCurrent()
    dc.takeAsicOffReset()
    dc.initializeAsic()
    val currentS2 = psc.measureCurrent()
    // activate bias generator
    dc.writeToAsicMemoryTop(0x2e, 0x0005)
    val currentS3 = psc.measureCurrent()
    // activate timing generators
    dc.writeToAsicMemoryTop(0x0a, 0x0040)
    dc.writeToAsicMemoryTop(0x0d, 0x0040)
    dc.writeToAsicMemoryTop(0x16, 0x000e)
    val currentS4 = psc.measureCurrent()
    // activate output timing generator
    dc.writeToAsicMemoryTop(0x5f, 0x0141)
    val currentS5 = psc.measureCurrent()
    // activate adc channels one by one
    // top1
    dc.writeToAsicMemoryTop(0x18, 0x0880)
    dc.writeToAsicMemoryTop(0x1b, 0x00ee)
    dc.writeToAsicMemoryTop(0x1c, 0x0000)
    dc.writeToAsicMemoryTop(0x1d, 0x0057)
    dc.writeToAsicMemoryTop(0x1e, 0x7778)
    val currentS6a = psc.measureCurrent()
    // top2
    dc.writeToAsicMemoryTop(0x18, 0x0cc0)
    dc.writeToAsicMemoryTop(0x1b, 0x0066)
    dc.writeToAsicMemoryTop(0x1c, 0x0000)
    dc.writeToAsicMemoryTop(0x1d, 0x0053)
    dc.writeToAsicMemoryTop(0x1e, 0x333c)
    val currentS6b = psc.measureCurrent()
    // top3
    dc.writeToAsicMemoryTop(0x18, 0x0ee0)
    dc.writeToAsicMemoryTop(0x1b, 0x0022)
    dc.writeToAsicMemoryTop(0x1c, 0x0000)
    dc.writeToAsicMemoryTop(0x1d, 0x0051)
    dc.writeToAsicMemoryTop(0x1e, 0x111e)
    val currentS6c = psc.measureCurrent()
    // top4
    dc.writeToAsicMemoryTop(0x18, 0x0ff0)
    dc.writeToAsicMemoryTop(0x1b, 0x0000)
    dc.writeToAsicMemoryTop(0x1c, 0x0000)
    dc.writeToAsicMemoryTop(0x1d, 0x0050)
    dc.writeToAsicMemoryTop(0x1e, 0x000f)
    val currentS6d = psc.measureCurrent()
    // bot1
    dc.writeToAsicMemoryTop(0x58, 0x0880)
    dc.writeToAsicMemoryTop(0x5b, 0x00ee)
    dc.writeToAsicMemoryTop(0x5c, 0x0000)
    dc.writeToAsicMemoryTop(0x5d, 0x0057)
    dc.writeToAsicMemoryTop(0x5e, 0x7778)
    val currentS6e = psc.measureCurrent()
    // bot2
    dc.writeToAsicMemoryTop(0x58, 0x0cc0)
    dc.writeToAsicMemoryTop(0x5b, 0x0066)
    dc.writeToAsicMemoryTop(0x5c, 0x0000)
    dc.writeToAsicMemoryTop(0x5d, 0x0053)
    dc.writeToAsicMemoryTop(0x5e, 0x333c)
    val currentS6f = psc.measureCurrent()
    // bot3
    dc.writeToAsicMemoryTop(0x58, 0x0ee0)
    dc.writeToAsicMemoryTop(0x5b, 0x0022)
    dc.writeToAsicMemoryTop(0x5c, 0x0000)
    dc.writeToAsicMemoryTop(0x5d, 0x0051)
    dc.writeToAsicMemoryTop(0x5e, 0x111e)
    val currentS6g = psc.measureCurrent()
    // bot4
    dc.writeToAsicMemoryTop(0x58, 0x0ff0)
    dc.writeToAsicMemoryTop(0x5b, 0x0000)
    dc.writeToAsicMemoryTop(0x5c, 0x0000)
    dc.writeToAsicMemoryTop(0x5d, 0x0050)
    dc.writeToAsicMemoryTop(0x5e, 0x000f)
    val currentS6h = psc.measureCurrent()
    // activate output stages one by one
    dc.writeToAsicMemoryTop(0x21, 0x0ffe)
    val currentS7a = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0ffc)
    val currentS7b = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0ff8)
    val currentS7c = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0ff0)
    val currentS7d = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0fe0)
    val currentS7e = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0fc0)
    val currentS7f = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0f80)
    val currentS7g = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0f00)
    val currentS7h = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0e00)
    val currentS7i = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0c00)
    val currentS7j = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0800)
    val currentS7k = psc.measureCurrent()
    dc.writeToAsicMemoryTop(0x21, 0x0000)
    val currentS7l = psc.measureCurrent()
    val currentSeq = Seq(
      currentS1, currentS2, currentS3, currentS4, currentS5,
      currentS6a, currentS6b, currentS6c, currentS6d,
      currentS6e, currentS6f, currentS6g, currentS6h,
      currentS7a, currentS7b, currentS7c, currentS7d,
      currentS7e, currentS7f, currentS7g, currentS7h,
      currentS7i, currentS7j, currentS7k, currentS7l
    )
    val results = rvc.checkPower(currentSeq)
    if (results.contains(false)) {
      (false, "Not Implemented Yet\n") // TODO: Specify error output format
    } else {
      (true, "")
    }
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