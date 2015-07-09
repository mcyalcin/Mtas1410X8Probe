package com.mikrotasarim.api.command

import com.mikrotasarim.api.device.DeviceInterface

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

  def writeToPixelProcessorMemory = _
  def putFpgaOnReset = _
  def takeFpgaOffReset = _
  def putAsicOnReset = _
  def takeAsicOffReset = _
  def putFifosOnReset = _
  def takeFifosOffReset(mask: Int) = _
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

  val flashFifoInPipe = 0x80

  val flashFifoOutPipe = 0xa0

  val writeToAsicMemoryTopCommand = 0xc0
  val readFromAsicMemoryTopCommand = 0xc1
  val writeToAsicMemoryBotCommand = 0xc2
  val readFromAsicMemoryBotCommand = 0xc3
  val writeToRoicMemoryCommand = 0xc5
  val readFromRoicMemoryCommand = 0xc6
  val initializeAsicCommand = 0xcc
}