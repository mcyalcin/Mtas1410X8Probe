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
  
  val bc = FpgaController

  private def currentControlTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def serialInterfaceTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def memoryTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def powerConsumptionTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def timingGeneratorTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def outputTimingGeneratorTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def outputChannelTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelFunctionalityTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def pgaFunctionalityTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def inputDriverTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def pgaGainTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelLinearityTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def adcChannelNoiseTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithoutRoic)
    (false, "Not Implemented Yet\n")
  }

  private def roicProgrammingTest(): (Boolean, String) = {
    bc.deployBitfile(bc.Bitfile.WithRoic)
    (false, "Not Implemented Yet\n")
  }

  def runAll(): Unit = {
    for (testCase <- testCases) testCase.runTest()
  }
}