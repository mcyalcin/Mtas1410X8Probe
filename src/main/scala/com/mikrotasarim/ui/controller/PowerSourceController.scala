package com.mikrotasarim.ui.controller

import jssc.{SerialPortList, SerialPort}

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

object PowerSourceController {

  var serialPort: SerialPort = _

  def MeasureCurrent() = {
    serialPort.writeString("MEAS:CURR?\n")
    Thread.sleep(300)
    serialPort.readString().toDouble
  }

  val comPortList = ObservableBuffer(SerialPortList.getPortNames.toList)
  val selectedComPort = StringProperty("")

  selectedComPort.onChange(SetSerialPort())

  def SetSerialPort(): Unit = {
    serialPort = new SerialPort(selectedComPort.value)
    openPort()
  }

  def openPort(): Unit = {
    serialPort.openPort()
    serialPort.setParams(9600, 8, 1, 0)
  }

  def closePort(): Unit = {
    serialPort.closePort()
  }

  def setLocal(): Unit = {
    serialPort.writeString("SYST:LOC\n")
  }

  def setRemote(): Unit = {
    serialPort.writeString("SYST:REM\n")
  }

  def outputOn(): Unit = {
    serialPort.writeString("OUTP ON\n")
  }

  def outputOff(): Unit = {
    serialPort.writeString("OUTP OFF\n")
  }
}
