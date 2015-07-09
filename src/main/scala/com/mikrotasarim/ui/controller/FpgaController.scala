package com.mikrotasarim.ui.controller

import com.mikrotasarim.api.command.AsicController
import com.mikrotasarim.api.device.{DeviceInterface, ConsoleMockDeviceInterface, OpalKellyInterface}
import com.mikrotasarim.ui.controller.FpgaController.Bitfile.Bitfile

import scalafx.beans.property.{BooleanProperty, StringProperty}

object FpgaController {

  var device: DeviceInterface = _
  var deviceController: AsicController = _

  object Bitfile extends Enumeration {
    type Bitfile = Value
    val None, WithoutRoic, WithRoic = Value
  }

  val bitfileMap = Map(
    Bitfile.WithoutRoic -> "withoutRoic.bit",
    Bitfile.WithRoic -> "withRoic.bit"
  )

  var deployedBitfile: Bitfile = Bitfile.None

  def deployBitfile(bf: Bitfile): Unit = {
    if (bf != deployedBitfile) {
      if (testMode.value) {
        device = new ConsoleMockDeviceInterface()
      } else {
        device = new OpalKellyInterface(bitfileMap(bf))
      }
      deviceController = new AsicController(device)
      deployedBitfile = bf
    }
  }

  def readStatusRegister(): Unit = {
    val status = deviceController.readStatusRegister()
    statusRegister.set(status.toString)
  }

  val statusRegister = StringProperty("")
  val testMode = BooleanProperty(value = false)
}
