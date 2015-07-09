package com.mikrotasarim.ui.controller

import com.mikrotasarim.api.command.AsicController
import com.mikrotasarim.api.device.OpalKellyInterface
import com.mikrotasarim.ui.controller.FpgaController.Bitfile.Bitfile

object FpgaController {

  var device: OpalKellyInterface = _
  var deviceController: AsicController = _

  object Bitfile extends Enumeration {
    type Bitfile = Value
    val None, WithoutRoic, WithRoic = Value
  }

  val bitfileMap = Map(
    Bitfile.WithoutRoic -> "withoutRoic.bit",
    Bitfile.WithRoic -> "withRoic.bit"
  )

  private var deployedBitfile: Bitfile = Bitfile.None

  def deployBitfile(bf: Bitfile): Unit = {
    if (bf != deployedBitfile) {
      device = new OpalKellyInterface(bitfileMap(bf))
      deviceController = new AsicController(device)
      deployedBitfile = bf
    }
  }
}
