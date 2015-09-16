package com.mikrotasarim.api.command

import com.mikrotasarim.api.device.DeviceInterface

import spire.implicits._

class AsicController(device: DeviceInterface) {

  import ApiConstants._

  private def checkErrorCode(errorCode: Long): Unit = {
    errorCode % 65536 match {
      case 0 =>
      case default => throw new Exception("Unexpected error code: " + default)
    }
  }

  def waitForDeviceReady(): Unit = {
    do {
      device.updateWireOuts()
    } while (device.getWireOutValue(statusWire) != 0)
  }

  private def setWiresAndTrigger(wires: Map[Int, Long]): Unit = {
    waitForDeviceReady()
    for (wire <- wires.keys) {
      device.setWireInValue(wire, wires(wire))
    }
    device.updateWireIns()
    device.activateTriggerIn(triggerWire, 0)
    device.updateWireOuts()
    val errorCode = device.getWireOutValue(errorWire)
    checkErrorCode(errorCode)
  }

  def initializeAsic(): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> initializeAsicCommand
    ))
    setWiresAndTrigger(Map(
      commandWire -> initializeAsicCommand
    ))
  }

  def writeToAsicMemoryTop(address: Int, value: Long): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> writeToAsicMemoryTopCommand,
      addressWire -> address,
      dataWire -> value
    ))
  }

  def writeToAsicMemoryTopMasked(address: Int, value: Long, mask: Long): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> writeToAsicMemoryTopCommand,
      addressWire -> address,
      dataWire -> (value + 0x100 * mask)
    ))
  }

  def writeToAsicMemoryBot(address: Int, value: Long): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> writeToAsicMemoryBotCommand,
      addressWire -> address,
      dataWire -> value
    ))
  }

  def readFromAsicMemoryTop(address: Int): Long = {
    setWiresAndTrigger(Map(
      commandWire -> readFromAsicMemoryTopCommand,
      addressWire -> address
    ))
    device.getWireOutValue(readWire)
  }

  def readFromAsicMemoryBot(address: Int): Long = {
    setWiresAndTrigger(Map(
      commandWire -> readFromAsicMemoryBotCommand,
      addressWire -> address
    ))
    device.getWireOutValue(readWire)
  }

  def writeToRoicMemory(address: Int, value: Long): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> writeToRoicMemoryCommand,
      addressWire -> address,
      dataWire -> value
    ))
  }

  def readFromRoicMemory(address: Int): Long = {
    setWiresAndTrigger(Map(
      commandWire -> readFromRoicMemoryCommand,
      addressWire -> address
    ))
    device.getWireOutValue(readWire)
  }

  def writeToPixelProcessorMemory(address: Int, value: Long): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> writeToPixelProcessorMemoryCommand,
      addressWire -> address,
      dataWire -> value
    ))
  }

  def putFpgaOnReset(): Unit = {
    device.setWireInValue(resetWire, 2 pow fpgaReset, 2 pow fpgaReset)
    device.updateWireIns()
  }

  def takeFpgaOffReset(): Unit = {
    device.setWireInValue(resetWire, 0, 2 pow fpgaReset)
    device.updateWireIns()
  }

  def putAsicOnReset(): Unit = {
    device.setWireInValue(resetWire, 2 pow asicReset, 2 pow asicReset)
    device.updateWireIns()
  }

  def takeAsicOffReset(): Unit = {
    device.setWireInValue(resetWire, 0, 2 pow asicReset)
    device.updateWireIns()
  }

  def setFifosResets(config: Int): Unit = {
    device.setWireInValue(resetWire, config * 2 pow fifoResetOffset, 0xff * 2 pow fifoResetOffset)
    device.updateWireIns()
  }

  def readStatusRegister(): Long = {
    setWiresAndTrigger(Map(
      commandWire -> readStatusRegisterCommand
    ))
    device.getWireOutValue(readWire)
  }

  def softResetAsic(): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> softResetAsicCommand
    ))
  }

  def softResetRoic(): Unit = {
    setWiresAndTrigger(Map(
      commandWire -> softResetRoicCommand
    ))
  }

  def readData(length: Int): Seq[Array[Long]] = {
    def convertToWords(bytes: Array[Byte]): Array[Long] = {
      (for (i <- 0 until length) yield {
        ((bytes((4 * i) + 0) + 256) % 256) +
        ((bytes((4 * i) + 1) + 256) % 256) * 256l +
        ((bytes((4 * i) + 2) + 256) % 256) * 256l * 256l +
        ((bytes((4 * i) + 3) + 256) % 256) * 256l * 256l * 256l
      }).toArray
    }

    val lengthInBytes = length * 4
    setWiresAndTrigger(Map(
      commandWire -> readDataCommand,
      addressWire -> 0xff,
      dataWire -> lengthInBytes
    ))
    val output: Array[Array[Byte]] = Array.ofDim[Byte](8, lengthInBytes)
    for (i <- 0 to 7) yield {
      device.readFromPipeOut(outputPipe(i), lengthInBytes, output(i))
      convertToWords(output(i))
    }
  }
}

object ApiConstants {

  val resetWire = 0x00
  val commandWire = 0x01
  val addressWire = 0x02
  val dataWire = 0x03
  val delayWire = 0x04

  val readWire = 0x20
  val errorWire = 0x21
  val statusWire = 0x22
  val readyWire = 0x23

  val triggerWire = 0x40

  val outputPipe = (i: Int) => i + 0xa0

  val writeToAsicMemoryTopCommand = 0xc0
  val readFromAsicMemoryTopCommand = 0xc1
  val writeToAsicMemoryBotCommand = 0xc2
  val readFromAsicMemoryBotCommand = 0xc3
  val writeToRoicMemoryCommand = 0xc5
  val readFromRoicMemoryCommand = 0xc6
  val writeToPixelProcessorMemoryCommand = 0xca
  val initializeAsicCommand = 0xcc
  val readStatusRegisterCommand = 0xb0
  val softResetAsicCommand = 0xa0
  val softResetRoicCommand = 0xa1
  val readDataCommand = 0xd0

  val fpgaReset = 0
  val asicReset = 1
  val fifoResetOffset = 8
}