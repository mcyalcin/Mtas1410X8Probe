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
      (false, read.toHexString + " read\n")
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
    reset()
    dc.initializeAsic()
    dc.writeToAsicMemoryTop(0x0a, 0x0040)
    dc.writeToAsicMemoryTop(0x0d, 0x0040)
    dc.writeToAsicMemoryTop(0x16, 0x000e)
    for (i <- 0 to 15) {
      val status = dc.readStatusRegister()
      if (status % 4 != 3) return (false, status + "\n")
    }
    (true, "")
  }

  private def outputTimingGeneratorTest(): (Boolean, String) = {
    val errors = new StringBuilder

    def statusFail = (for (i <- 0 to 15) yield {
      val status = dc.readStatusRegister()
      if ((status / 4) % 2 != 1) true else false
    }).contains(true)

    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0041, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0012, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 80 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0042, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0018, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 100 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0044, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0026, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 140 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0045, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x002e, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 160 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0082, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0038, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0x0202)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 200 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0085, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0058, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0x0205)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 320 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0087, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0085, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0x0205)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 400 Mhz\n")

    dc.writeToAsicMemoryTopMasked(0x95, 0x0000, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x95, 0x008b, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x00c9, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0x0207)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    if (statusFail) errors.append("Failed at 560 Mhz\n")

    (errors.toString().isEmpty, errors.toString())
  }

  private def outputChannelTest(): (Boolean, String) = {
    val errors = new StringBuilder

    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    // stage1
    dc.writeToAsicMemoryTop(0x1f, 0xaa3c)
    dc.writeToAsicMemoryTop(0x2c, 0x552d)
    dc.writeToAsicMemoryTopMasked(0x28, 0x0800, 0x0800)
    dc.setFifosResets(0xff)
    val outputs = dc.readData(1)
    for (i <- 0 until 3) {
      if (outputs(i)(0) != 0xaa3c) errors.append("Top " + i + " fail: " + outputs(i)(0).toHexString + " read, 0xaa3c expected\n")
    }
    for (i <- 0 until 3) {
      if (outputs(i + 4)(0) != 0x552d) errors.append("Bottom " + i + " fail: " + outputs(i + 4)(0).toHexString + " read, 0x552d expected\n")
    }
    // stage2
    dc.writeToAsicMemoryTopMasked(0x95, 0x0041, 0x00ff)
    dc.writeToAsicMemoryTopMasked(0x96, 0x0012, 0x00ff)
    dc.writeToAsicMemoryTop(0x102, 0)
    dc.writeToAsicMemoryTopMasked(0x95, 0x0100, 0x0100)
    dc.writeToAsicMemoryTopMasked(0x40, 0x0000, 0xff00)
    dc.writeToAsicMemoryTop(0x24, 0x0f00)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0084)
    dc.writeToAsicMemoryTop(0x29, 0x000f)
    dc.writeToAsicMemoryTop(0x30, 0x0f0f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0108)
    dc.writeToAsicMemoryTop(0x93, 0x0050)
    dc.writeToAsicMemoryTop(0x94, 0x000f)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.setFifosResets(0xff)

    val referenceValues = Seq(
      3686, 5939, 10444, 12697, 14950, 11571, 4812, 1433
    )

    for (muxShift <- 0 to 7) {
      for (outputIndex <- 0 to 7) {
        dc.writeToPixelProcessorMemory(2*outputIndex, (outputIndex - muxShift) % 8)
      }
      val output = dc.readData(16)
      for (i <- 0 to 7) {
        val out = output(i).sum / 16
        val ref = referenceValues(i - muxShift)
        if (math.abs(out - ref) > 500) errors.append("Output " + i + " read " + out + " expected " + ref + " at mux shift " + muxShift + "\n")
      }
    }
    (errors.toString().isEmpty, errors.toString())
  }

  private def adcChannelFunctionalityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0f00)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0084)
    dc.writeToAsicMemoryTop(0x29, 0x000f)
    dc.writeToAsicMemoryTop(0x30, 0x0f0f)
    dc.writeToAsicMemoryTop(0x88, 0x0f00)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0084)
    dc.writeToAsicMemoryTop(0x93, 0x000f)
    dc.writeToAsicMemoryTop(0x94, 0x0f0f)
    dc.setFifosResets(0xff)

    val out = dc.readData(16).map(l => l.sum / 16)
    val ref = Seq(3686, 5939, 10444, 12697, 12697, 10444, 5939, 3686)
    val errors = new StringBuilder
    for (i <- 0 to 7) if (math.abs(out(i) - ref(i)) > 500) errors.append("Output " + i + " read " + out(i) + " expected " + ref(i) + "\n")
    (errors.toString().isEmpty, errors.toString())
  }

  private def pgaFunctionalityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0108)
    dc.writeToAsicMemoryTop(0x29, 0x0050)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0108)
    dc.writeToAsicMemoryTop(0x93, 0x0050)
    dc.writeToAsicMemoryTop(0x94, 0x000f)
    dc.setFifosResets(0xff)

    val out = dc.readData(16).map(l => l.sum / 16)
    val ref = Seq(1433, 4812, 11571, 14950, 14950, 11571, 4812, 1433)
    val errors = new StringBuilder
    for (i <- 0 to 7) if (math.abs(out(i) - ref(i)) > 500) errors.append("Output " + i + " read " + out(i) + " expected " + ref(i) + "\n")
    (errors.toString().isEmpty, errors.toString())
  }

  private def inputDriverTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.setFifosResets(0xff)

    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0f00)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x0000)
    dc.writeToAsicMemoryTop(0x28, 0x0210)
    dc.writeToAsicMemoryTop(0x29, 0x000f)
    dc.writeToAsicMemoryTop(0x30, 0x0f0f)
    dc.writeToAsicMemoryTop(0x88, 0x0f00)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x0000)
    dc.writeToAsicMemoryTop(0x92, 0x0210)
    dc.writeToAsicMemoryTop(0x93, 0x000f)
    dc.writeToAsicMemoryTop(0x94, 0x0f0f)

    val out = dc.readData(16).map(l => l.sum / 16)
    val ref = Seq(3686, 5939, 10444, 12697, 12697, 10444, 5939, 3686)
    val errors = new StringBuilder
    for (i <- 0 to 7) if (math.abs(out(i) - ref(i)) > 500) errors.append("Output " + i + " read " + out(i) + " expected " + ref(i) + " in Stage 1\n")

    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x0000)
    dc.writeToAsicMemoryTop(0x28, 0x0000)
    dc.writeToAsicMemoryTop(0x29, 0x0050)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x0000)
    dc.writeToAsicMemoryTop(0x92, 0x0000)
    dc.writeToAsicMemoryTop(0x93, 0x0050)
    dc.writeToAsicMemoryTop(0x94, 0x000f)

    val out2 = dc.readData(16).map(l => l.sum / 16)
    val ref2 = Seq(1433, 4812, 11571, 14950, 14950, 11571, 4812, 1433)
    for (i <- 0 to 7) if (math.abs(out2(i) - ref2(i)) > 500) errors.append("Output " + i + " read " + out2(i) + " expected " + ref2(i) + " in Stage 2\n")
    (errors.toString().isEmpty, errors.toString())
  }

  private def pgaGainTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.setFifosResets(0xff)

    val out = Array.ofDim[Long](3,3,8)
    val ref = Array(Array(6182, 5177, 4172), Array(8192, 8192, 8192), Array(10311, 11371, 12431))
    val errors = new StringBuilder

    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x00f0)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x00f0)
    dc.writeToAsicMemoryTop(0x94, 0x000f)

    dc.writeToAsicMemoryTop(0x86, 0x5623)
    out(0)(0) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x58ba)
    out(0)(1) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x5b79)
    out(0)(2) = dc.readData(16).map(l => l.sum / 16).toArray

    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x0050)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x0050)
    dc.writeToAsicMemoryTop(0x94, 0x000f)

    dc.writeToAsicMemoryTop(0x86, 0x5623)
    out(1)(0) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x58ba)
    out(1)(1) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x5b79)
    out(1)(2) = dc.readData(16).map(l => l.sum / 16).toArray

    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x0000)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x0000)
    dc.writeToAsicMemoryTop(0x94, 0x000f)

    dc.writeToAsicMemoryTop(0x86, 0x5623)
    out(2)(0) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x58ba)
    out(2)(1) = dc.readData(16).map(l => l.sum / 16).toArray
    dc.writeToAsicMemoryTop(0x86, 0x5b79)
    out(2)(2) = dc.readData(16).map(l => l.sum / 16).toArray

    val g = Map(0 -> "low", 1 -> "medium", 2 -> "high")

    for (stage <- 0 to 2)
      for (gain <- 0 to 2)
        for (output <- 0 to 7) {
          val o = out(stage)(gain)(output)
          val r = ref(stage)(gain)
          if (math.abs(o-r) > 200) errors.append("Output " + output + " read " + o + " expected " + r + " at stage " + (stage + 1) + " with " + g(gain) + " gain\n")
        }

    (errors.toString().isEmpty, errors.toString())
  }

  private def adcChannelLinearityTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.setFifosResets(0xff)

    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)
    dc.writeToAsicMemoryTop(0x24, 0x0ff0)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01fe)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x0000)
    dc.writeToAsicMemoryTop(0x30, 0x000f)
    dc.writeToAsicMemoryTop(0x88, 0x0ff0)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01fe)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x0000)
    dc.writeToAsicMemoryTop(0x94, 0x000f)

    val out = for (dacValue <- 0x5000 until 0x5fff by 10) yield {
      dc.writeToAsicMemoryTop(86, dacValue)
      dc.readData(16).map(l => l.sum / 16)
    }

    ReferenceValueController.checkAdcLinearity(out)
  }

  private def adcChannelNoiseTest(): (Boolean, String) = {

    def mean(xs: List[Long]): Double = xs match {
      case Nil => 0.0
      case ys => ys.sum / ys.size.toDouble
    }

    def stddev(xs: List[Long], avg: Double): Double = xs match {
      case Nil => 0.0
      case ys => math.sqrt((0.0 /: ys) {
        (a,e) => a + math.pow(e - avg, 2.0)
      } / xs.size)
    }

    val out = Array.ofDim[Double](8)

    fc.deployBitfile(fc.Bitfile.WithoutRoic)
    reset()
    dc.initializeAsic()
    dc.setFifosResets(0xff)

    dc.writeToAsicMemoryTop(0x46, 0x0005)
    dc.writeToAsicMemoryTop(0x10, 0x0040)
    dc.writeToAsicMemoryTop(0x13, 0x0040)
    dc.writeToAsicMemoryTop(0x22, 0x000e)
    dc.writeToAsicMemoryTop(0x95, 0x0141)

    dc.writeToAsicMemoryTop(0x85, 0x7000)
    dc.writeToAsicMemoryTop(0x86, 0x7000)

    dc.writeToAsicMemoryTop(0x24, 0x0880)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01ff)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x0057)
    dc.writeToAsicMemoryTop(0x30, 0x7778)
    dc.writeToAsicMemoryTop(0x88, 0x0880)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01ff)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x0057)
    dc.writeToAsicMemoryTop(0x94, 0x7778)

    val read3 = dc.readData(1024)

    out(3) = stddev(read3(3).toList, mean(read3(3).toList))
    out(7) = stddev(read3(7).toList, mean(read3(7).toList))

    dc.writeToAsicMemoryTop(0x24, 0x0440)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01ff)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x005b)
    dc.writeToAsicMemoryTop(0x30, 0xbbb4)
    dc.writeToAsicMemoryTop(0x88, 0x0440)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01ff)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x005b)
    dc.writeToAsicMemoryTop(0x94, 0xbbb4)

    val read2 = dc.readData(1024)

    out(2) = stddev(read2(2).toList, mean(read2(2).toList))
    out(6) = stddev(read2(6).toList, mean(read2(6).toList))

    dc.writeToAsicMemoryTop(0x24, 0x0220)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01ff)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x005d)
    dc.writeToAsicMemoryTop(0x30, 0xddd2)
    dc.writeToAsicMemoryTop(0x88, 0x0220)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01ff)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x005d)
    dc.writeToAsicMemoryTop(0x94, 0xddd2)

    val read1 = dc.readData(1024)

    out(1) = stddev(read1(1).toList, mean(read1(1).toList))
    out(5) = stddev(read1(5).toList, mean(read1(5).toList))

    dc.writeToAsicMemoryTop(0x24, 0x0110)
    dc.writeToAsicMemoryTop(0x25, 0x0000)
    dc.writeToAsicMemoryTop(0x26, 0x0233)
    dc.writeToAsicMemoryTop(0x27, 0x01ff)
    dc.writeToAsicMemoryTop(0x28, 0x0042)
    dc.writeToAsicMemoryTop(0x29, 0x005e)
    dc.writeToAsicMemoryTop(0x30, 0xeee1)
    dc.writeToAsicMemoryTop(0x88, 0x0110)
    dc.writeToAsicMemoryTop(0x89, 0x0000)
    dc.writeToAsicMemoryTop(0x90, 0x0233)
    dc.writeToAsicMemoryTop(0x91, 0x01ff)
    dc.writeToAsicMemoryTop(0x92, 0x0042)
    dc.writeToAsicMemoryTop(0x93, 0x005e)
    dc.writeToAsicMemoryTop(0x94, 0xeee1)

    val read0 = dc.readData(1024)

    out(0) = stddev(read0.head.toList, mean(read0.head.toList))
    out(4) = stddev(read0(4).toList, mean(read0(4).toList))

    val errors = new StringBuilder

    for (i <- out.indices) if (out(i) > ReferenceValueController.noiseThreshold) errors.append("Channel " + i + " noise " + out(i) + "\n")

    (errors.toString().isEmpty, errors.toString())
  }

  private def roicProgrammingTest(): (Boolean, String) = {
    fc.deployBitfile(fc.Bitfile.WithRoic)
    reset()
    dc.initializeAsic()

    val out0 = dc.readFromRoicMemory(0x0f)
    dc.writeToRoicMemory(0x0f, 0xabcd)
    val out1 = dc.readFromRoicMemory(0x0f)

    val errors = new StringBuilder

    if (out0 != 0) errors.append("First read " + out0 + "\n")
    if (out1 != 0xabcd) errors.append("Second read " + out1 + "\n")

    (errors.toString().isEmpty, errors.toString())
  }

  def runAll(): Unit = {
    for (testCase <- testCases) testCase.runTest()
  }
}