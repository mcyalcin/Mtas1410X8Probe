package com.mikrotasarim.ui.view

import com.mikrotasarim.ui.model.MemoryMap
import com.mikrotasarim.ui.model.MemoryMap.MemoryLocation

import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.stage.Stage

object MemoryMapStage extends Stage {

  width = 800
  height = 600
  title = "Memory Map"
  scene = new Scene() {
    root = new ScrollPane {
      content = new HBox {
        spacing = 20
        content = List(
          new VBox {
            padding = Insets(10)
            spacing = 2
            content = new Label("Top") +: (new Separator +: MemoryMap.memoryLocationsTop.map(createMemoryLocationControl))
          },
          new VBox {
            padding = Insets(10)
            spacing = 2
            content = new Label("Bottom") +: (new Separator +: MemoryMap.memoryLocationsBot.map(createMemoryLocationControl))
          }
        )
      }
    }
  }

  def createMemoryLocationControl(model: MemoryLocation): Node = new HBox {
    spacing = 10
    content = List(
      new Label("Addr: " + model.address) {
        prefWidth = 75
      },
      new TextField {
        text <==> model.text
      },
      new Button("Commit") {
        onAction = handle {
          model.commit()
        }
      }
    )
  }
}
