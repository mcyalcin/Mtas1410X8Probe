package com.mikrotasarim.ui.controller

import com.mikrotasarim.api.command.AsicController
import com.mikrotasarim.api.device.{DeviceInterface, ConsoleMockDeviceInterface, OpalKellyInterface}

import scalafx.beans.property.{BooleanProperty, StringProperty}

object FpgaController {

  var device: DeviceInterface = _
  var deviceController: AsicController = _

  var deployedBitfile = "none"

  def deployBitfile(bf: String): Unit = {
    if (bf != deployedBitfile) {
      if (testMode.value) {
        device = new ConsoleMockDeviceInterface()
      } else {
        device = new OpalKellyInterface(bf)
      }
      deviceController = new AsicController(device)
      deployedBitfile = bf
    }
  }

  def readStatusRegister(): Unit = {
    if (deployedBitfile == "none") deployBitfile("withoutRoic.bit")
    val status = deviceController.readStatusRegister()
    statusRegister.set(status.toString)
  }

  val statusRegister = StringProperty("")
  val testMode = BooleanProperty(value = false)
}
