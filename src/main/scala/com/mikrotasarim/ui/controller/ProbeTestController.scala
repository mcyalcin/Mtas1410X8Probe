package com.mikrotasarim.ui.controller

import com.mikrotasarim.ui.model.ProbeTestCase

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

  private def currentControlTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    dc.takeFpgaOffReset()
    dc.takeAsicOffReset()
    val current = psc.measureCurrent()
    if (rvc.checkCurrent(current)) {
      (true, "")
    } else {
      (false, current + " A\n")
    }
  }

  private def serialInterfaceTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def memoryTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
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