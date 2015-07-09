package com.mikrotasarim.ui.model

import com.mikrotasarim.ui.controller.FpgaController

import scalafx.beans.property.StringProperty

import com.mikrotasarim.ui.controller.FpgaController.{Bitfile, deviceController}

object MemoryMap {

  val memoryLocationsTop = for (i <- 0 to 255) yield new MemoryLocationTop(i)
  val memoryLocationsBot = for (i <- 0 to 128) yield new MemoryLocationBot(i)

  def readAsicMemory(): Unit = {
    if (FpgaController.deployedBitfile == Bitfile.None) {
      FpgaController.deployBitfile(Bitfile.WithoutRoic)
    }
    for (memoryLocation <- memoryLocationsTop) {
      memoryLocation.read()
    }
    for (memoryLocation <- memoryLocationsBot) {
      memoryLocation.read()
    }
  }

  abstract class MemoryLocation {
    val text = StringProperty("0000000000000000")

    val address: Int

    def read(): Unit

    def commit(): Unit
  }

  class MemoryLocationTop(addr: Int) extends MemoryLocation {
    val address = addr

    def read(): Unit = {
      val value = deviceController.readFromAsicMemoryTop(address)
      text.value = String.format("%16s", value.toBinaryString).replace(' ', '0')
    }

    def commit(): Unit = {
      val value = java.lang.Long.parseLong(text.value, 2)
      deviceController.writeToAsicMemoryTop(address, value)
      read()
    }
  }

  class MemoryLocationBot(val address: Int) extends MemoryLocation {
    def read(): Unit = {
      val value = deviceController.readFromAsicMemoryBot(address)
      text.value = String.format("%16s", value.toBinaryString).replace(' ', '0')
    }

    def commit(): Unit = {
      val value = java.lang.Long.parseLong(text.value, 2)
      deviceController.writeToAsicMemoryBot(address, value)
      read()
    }
  }
}
